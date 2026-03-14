use anyhow::Context;
use diqwest::{Credentials, WithDigestAuth};
use leaky_bucket::RateLimiter;
use log::{debug, info};
use quick_xml::Writer;
use quick_xml::events::{BytesEnd, BytesStart, BytesText, Event};
use reqwest::{Client, Url, header};
use std::collections::HashMap;
use std::fmt;
use std::io::Cursor;
use std::time::Duration;
use uuid::Uuid;

use crate::http::USER_AGENT;

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
pub struct UserTag {
	name: String,
}

impl UserTag {
	pub fn new(name: String) -> Self {
		UserTag { name }
	}
}

pub type ReleaseGroupTags = HashMap<Uuid, Vec<UserTag>>;

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
			.interval(Duration::from_millis(1500))
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

	pub async fn submit_release_group_tags(
		&self,
		results: ReleaseGroupTags,
	) -> anyhow::Result<reqwest::Response> {
		let mut url = self.base_url.clone();
		url.set_path("/ws/2/tag");

		let body = MusicbrainzClient::serialize_release_group_tags(results)?;
		debug!("Created body:\n{:?}", String::from_utf8(body.clone())?);

		self.limiter.acquire_one().await;

		debug!("Submitting tags...");
		let response = self
			.client
			.post(url)
			.header(header::CONTENT_TYPE, "application/xml; charset=UTF-8")
			.body(body)
			.send_digest_auth(Credentials::new(
				&self.credentials.username,
				&self.credentials.password,
			))
			.await?;
		info!("Submitted tags.");

		response.error_for_status().context("Failed to submit tags")
	}

	// https://musicbrainz.org/doc/MusicBrainz_API#Submitting_data
	pub(crate) fn serialize_release_group_tags(
		release_group_tags: ReleaseGroupTags,
	) -> anyhow::Result<Vec<u8>> {
		let mut writer = Writer::new(Cursor::new(Vec::new()));

		let mut root_node = BytesStart::new("metadata");
		root_node.push_attribute(("xmlns", "http://musicbrainz.org/ns/mmd-2.0#"));
		writer.write_event(Event::Start(root_node))?;
		writer.write_event(Event::Start(BytesStart::new("release-group-list")))?;

		for (target_mbid, tags) in release_group_tags {
			let mut artist = BytesStart::new("release-group");
			artist.push_attribute(("id", target_mbid.to_string().as_str()));
			writer.write_event(Event::Start(artist))?;
			writer.write_event(Event::Start(BytesStart::new("user-tag-list")))?;

			for tag in tags {
				writer.write_event(Event::Start(BytesStart::new("user-tag")))?;
				writer.write_event(Event::Start(BytesStart::new("name")))?;
				writer.write_event(Event::Text(BytesText::new(&tag.name)))?;
				writer.write_event(Event::End(BytesEnd::new("name")))?;
				writer.write_event(Event::End(BytesEnd::new("user-tag")))?;
			}

			writer.write_event(Event::End(BytesEnd::new("user-tag-list")))?;
			writer.write_event(Event::End(BytesEnd::new("release-group")))?;
		}

		writer.write_event(Event::End(BytesEnd::new("release-group-list")))?;
		writer.write_event(Event::End(BytesEnd::new("metadata")))?;

		Ok(writer.into_inner().into_inner())
	}
}

#[cfg(test)]
mod tests {
	use std::str::FromStr;
	use uuid::Uuid;

	use super::*;

	#[test]
	fn serialize_release_group_tags_serializes() -> anyhow::Result<()> {
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

		let mut release_group_tags: ReleaseGroupTags = HashMap::new();
		release_group_tags.insert(
			Uuid::from_str("b1392450-e666-3926-a536-22c65f834433")?,
			vec![
				UserTag::new("rock".to_string()),
				UserTag::new("afoxê".to_string()),
				UserTag::new("yé-yé".to_string()),
			],
		);
		release_group_tags.insert(
			Uuid::from_str("e75c0549-ad55-39e3-8025-c72c5d4a3c5d")?,
			vec![UserTag::new("rock".to_string())],
		);

		let actual = String::from_utf8(MusicbrainzClient::serialize_release_group_tags(
			release_group_tags,
		)?)?;

		assert_eq!(actual, expected);

		Ok(())
	}
}
