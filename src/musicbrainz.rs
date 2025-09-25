use crate::sql::ReleaseGroupEnrichmentMergedResult;
use anyhow::{Context, Ok};
use diqwest::WithDigestAuth;
use leaky_bucket::RateLimiter;
use log::debug;
use quick_xml::Writer;
use quick_xml::events::{BytesEnd, BytesStart, BytesText, Event};
use reqwest::{Client, Url, header};
use std::fmt;
use std::io::Cursor;
use std::time::Duration;

// See https://musicbrainz.org/doc/MusicBrainz_API
const USER_AGENT: &str = concat!(
	env!("CARGO_PKG_NAME"),
	"/",
	env!("CARGO_PKG_VERSION"),
	" (",
	env!("CARGO_PKG_HOMEPAGE"),
	" )",
);

// See https://musicbrainz.org/doc/MusicBrainz_API, different from user agent
const CLIENT_NAME: &str = concat!(env!("CARGO_PKG_NAME"), "-", env!("CARGO_PKG_VERSION"));

#[derive(Debug)]
pub struct MusicbrainzCredentials {
	pub username: String,
	pub password: String,
}

impl fmt::Display for MusicbrainzCredentials {
	fn fmt(&self, f: &mut fmt::Formatter<'_>) -> fmt::Result {
		write!(f, "(username={}, password=<REDACTED>)", self.username)
	}
}

#[derive(Debug)]
pub struct MusicbrainzClient {
	base_url: Url,
	credentials: MusicbrainzCredentials,
	client: Client,
	limiter: RateLimiter,
}

impl MusicbrainzClient {
	pub fn new(
		musicbrainz_url: Url,
		credentials: MusicbrainzCredentials,
	) -> anyhow::Result<MusicbrainzClient> {
		let mut base_url = musicbrainz_url;
		base_url.set_query(Some(&format!("client={CLIENT_NAME}")));

		let client = Client::builder().user_agent(USER_AGENT).build()?;

		// See per-IP-address limit https://musicbrainz.org/doc/MusicBrainz_API/Rate_Limiting,
		// further slowed down to adapt for network fluctuations.
		let limiter = RateLimiter::builder()
			.interval(Duration::from_secs(2))
			.refill(1)
			.initial(1)
			.build();

		Ok(MusicbrainzClient {
			base_url,
			credentials,
			client,
			limiter,
		})
	}

	pub async fn submit_tags(
		&self,
		data: Vec<ReleaseGroupEnrichmentMergedResult>,
	) -> anyhow::Result<reqwest::Response> {
		let mut url = self.base_url.clone();
		url.set_path("/ws/2/tag");

		let body = serialize_tags(data.into_iter())?;

		self.limiter.acquire_one().await;

		debug!("Submitting tags...");
		let response = self
			.client
			.post(url)
			.header(header::CONTENT_TYPE, "application/xml; charset=UTF-8")
			.body(body)
			.send_with_digest_auth(&self.credentials.username, &self.credentials.password)
			.await?;
		debug!("Submitted tags.");

		response.error_for_status().context("Failed to submit tags")
	}
}

fn serialize_tags<T>(tags_by_release_group: T) -> anyhow::Result<Vec<u8>>
where
	T: Iterator<Item = ReleaseGroupEnrichmentMergedResult>,
{
	let mut buf = Cursor::new(Vec::new());
	let mut writer = Writer::new(&mut buf);

	let mut root = BytesStart::new("metadata");
	root.push_attribute(("xmlns", "http://musicbrainz.org/ns/mmd-2.0#"));
	writer.write_event(Event::Start(root))?;
	writer.write_event(Event::Start(BytesStart::new("release-group-list")))?;

	for release_group in tags_by_release_group {
		let mut artist = BytesStart::new("release-group");
		artist.push_attribute(("id", release_group.target_mbid.to_string().as_str()));
		writer.write_event(Event::Start(artist))?;
		writer.write_event(Event::Start(BytesStart::new("user-tag-list")))?;

		for genre in &release_group.genres {
			writer.write_event(Event::Start(BytesStart::new("user-tag")))?;
			writer.write_event(Event::Start(BytesStart::new("name")))?;
			writer.write_event(Event::Text(BytesText::new(genre)))?;
			writer.write_event(Event::End(BytesEnd::new("name")))?;
			writer.write_event(Event::End(BytesEnd::new("user-tag")))?;
		}

		writer.write_event(Event::End(BytesEnd::new("user-tag-list")))?;
		writer.write_event(Event::End(BytesEnd::new("release-group")))?;
	}

	writer.write_event(Event::End(BytesEnd::new("release-group-list")))?;
	writer.write_event(Event::End(BytesEnd::new("metadata")))?;

	Ok(buf.into_inner())
}

#[cfg(test)]
mod tests {
	use std::str::FromStr;
	use uuid::Uuid;

	use super::*;

	#[test]
	fn serialize_tags_serializes() -> anyhow::Result<()> {
		let expected = concat!(
			"<metadata xmlns=\"http://musicbrainz.org/ns/mmd-2.0#\"><release-group-list>",
			"<release-group id=\"b1392450-e666-3926-a536-22c65f834433\"><user-tag-list>",
			"<user-tag><name>rock</name></user-tag>",
			"<user-tag><name>afoxê</name></user-tag>",
			"<user-tag><name>yé-yé</name></user-tag>",
			"</user-tag-list></release-group>",
			"<release-group id=\"e75c0549-ad55-39e3-8025-c72c5d4a3c5d\"><user-tag-list>",
			"<user-tag><name>rock</name></user-tag>",
			"</user-tag-list></release-group>",
			"</release-group-list></metadata>"
		);

		let tags_by_release_group = vec![
			ReleaseGroupEnrichmentMergedResult {
				target_mbid: Uuid::from_str("b1392450-e666-3926-a536-22c65f834433")?,
				genres: vec!["rock".to_string(), "afoxê".to_string(), "yé-yé".to_string()],
			},
			ReleaseGroupEnrichmentMergedResult {
				target_mbid: Uuid::from_str("e75c0549-ad55-39e3-8025-c72c5d4a3c5d")?,
				genres: vec!["rock".to_string()],
			},
		];

		let actual = String::from_utf8(serialize_tags(tags_by_release_group.into_iter())?)?;

		assert_eq!(actual, expected);

		Ok(())
	}
}
