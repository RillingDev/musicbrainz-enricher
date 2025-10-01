use std::{collections::HashSet, time::Duration};

use anyhow::Ok;
use log::{debug, info};
use reqwest::Url;

use crate::gather::gatherer::discogs::{DiscogsClient, DiscogsCredentials};

pub mod discogs;

#[derive(Debug)]
pub enum ReleaseGroupGatherer {
	Discogs(DiscogsClient),
	Dummy,
}

impl ReleaseGroupGatherer {
	fn supported_hosts(&self) -> HashSet<String> {
		match self {
			ReleaseGroupGatherer::Discogs(_) => DiscogsClient::supported_hosts(),
			ReleaseGroupGatherer::Dummy => HashSet::from(["open.spotify.com".to_string()]),
		}
	}

	async fn gather_genres(&self, entity_url: &Url) -> anyhow::Result<Vec<String>> {
		match self {
			ReleaseGroupGatherer::Discogs(discogs_client) => {
				discogs_client.gather_genres(entity_url).await
			}
			ReleaseGroupGatherer::Dummy => {
				let () = tokio::time::sleep(Duration::from_secs(1)).await;
				Ok(["fizz".to_string(), "rock".to_string()].to_vec())
			}
		}
	}
}

#[derive(Debug)]
pub struct ReleaseGroupGatherService {
	gatherers: Vec<ReleaseGroupGatherer>,
}

impl ReleaseGroupGatherService {
	pub fn new(discogs_credentials: Option<DiscogsCredentials>) -> anyhow::Result<Self> {
		let discogs_client = DiscogsClient::new(discogs_credentials)?;

		Ok(ReleaseGroupGatherService {
			gatherers: vec![
				ReleaseGroupGatherer::Dummy,
				ReleaseGroupGatherer::Discogs(discogs_client),
			],
		})
	}

	pub async fn gather_genres(&self, entity_url: &str) -> anyhow::Result<Vec<String>> {
		let parsed_url = Url::parse(entity_url)?;
		let host = parsed_url.host_str().unwrap_or("");

		if let Some(gatherer) = self
			.gatherers
			.iter()
			.find(|g| g.supported_hosts().contains(host))
		{
			let result = gatherer.gather_genres(&parsed_url).await;
			info!("Gathered {result:?} for URL '{entity_url}'.");
			result
		} else {
			debug!("No gatherer found for URL '{entity_url}'.");
			Ok(Vec::new())
		}
	}
}
