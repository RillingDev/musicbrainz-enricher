use std::{collections::HashSet, fmt, sync::LazyLock, time::Duration};

use crate::http::USER_AGENT;
use anyhow::Ok;
use leaky_bucket::RateLimiter;
use log::warn;
use regex::Regex;

use reqwest::{Client, ClientBuilder, Url, header};
use serde::Deserialize;

#[derive(Debug)]
pub struct DiscogsCredentials {
	pub token: String,
}

impl fmt::Display for DiscogsCredentials {
	fn fmt(&self, f: &mut fmt::Formatter<'_>) -> fmt::Result {
		write!(f, "(token=<REDACTED>)")
	}
}

#[derive(Debug)]
pub struct DiscogsClient {
	base_url: Url,
	client: Client,
	limiter: RateLimiter,
}

#[derive(Debug, Deserialize)]
struct DiscogsReleaseGroup {
	genres: HashSet<String>,
	styles: HashSet<String>,
}

static RELEASE_GROUP_ID_PATTERN: LazyLock<Regex> =
	LazyLock::new(|| Regex::new("/master/(?<id>\\d+)").expect("Regex construction failed."));

// See https://www.discogs.com/developers/
impl DiscogsClient {
	pub fn new(credentials: Option<DiscogsCredentials>) -> Result<Self, anyhow::Error> {
		let base_url = Url::parse("https://api.discogs.com")?;

		// See https://www.discogs.com/developers/#page:home,header:home-rate-limiting,
		// further slowed down to adapt for network fluctuations.
		let limiter = RateLimiter::builder()
			.interval(Duration::from_secs(15))
			.refill(match credentials {
				Some(_) => 5,
				None => 15,
			})
			.initial(5)
			.build();

		let mut headers = header::HeaderMap::new();
		if let Some(cred) = credentials {
			headers.insert(
				header::AUTHORIZATION,
				header::HeaderValue::from_str(&format!("Discogs token={}", cred.token))?,
			);
		}

		let client = ClientBuilder::new()
			.user_agent(USER_AGENT)
			.default_headers(headers)
			.build()?;

		Ok(DiscogsClient {
			base_url,
			client,
			limiter,
		})
	}

	async fn lookup_release_group(&self, id: &str) -> anyhow::Result<DiscogsReleaseGroup> {
		let mut url = self.base_url.clone();
		url.set_path(format!("masters/{id}").as_str());

		self.limiter.acquire_one().await;

		let release_group = self
			.client
			.get(url)
			.header(header::ACCEPT, "application/json")
			.send()
			.await?
			.error_for_status()?
			.json::<DiscogsReleaseGroup>()
			.await?;
		Ok(release_group)
	}

	pub fn supported_hosts() -> HashSet<String> {
		HashSet::from(["www.discogs.com".to_string()])
	}

	pub async fn gather_genres(&self, entity_url: &Url) -> anyhow::Result<Vec<String>> {
		if let Some(captures) = RELEASE_GROUP_ID_PATTERN.captures(entity_url.path()) {
			let id = &captures["id"];
			self.lookup_release_group(id).await.map(|release_group| {
				release_group
					.genres
					.union(&release_group.styles)
					.map(Clone::clone)
					.collect()
			})
		} else {
			warn!("Could not extract ID from URL '{entity_url}'.");
			Ok(Vec::new())
		}
	}
}

#[cfg(test)]
mod tests {

	use super::*;

	#[tokio::test]
	#[ignore]
	async fn gather_genres_manual_test() -> anyhow::Result<()> {
		let client = DiscogsClient::new(None)?;
		let url = Url::parse("https://www.discogs.com/master/96559")?;

		let result = client.gather_genres(&url).await;
		println!("Result is {result:?}");
		assert!(result.is_ok());

		Ok(())
	}
}
