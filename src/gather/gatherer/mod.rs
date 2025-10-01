use anyhow::Ok;
use log::{debug, info};
use reqwest::Url;

use crate::gather::gatherer::{
	discogs::{DiscogsClient, DiscogsCredentials},
	dummy::DummyGatherer,
};

pub mod discogs;
pub mod dummy;

pub trait ReleaseGroupGatherer {
	fn can_gather(&self, entity_url: &Url) -> bool;

	async fn gather_genres(&self, entity_url: &Url) -> anyhow::Result<Vec<String>>;
}
pub struct ReleaseGroupGatherService {
	// TODO
	pub gatherers: Vec<DummyGatherer>,
}

impl ReleaseGroupGatherService {
	pub fn new(discogs_credentials: Option<DiscogsCredentials>) -> anyhow::Result<Self> {
		// TODO
		let discogs_client = DiscogsClient::new(discogs_credentials);

		Ok(ReleaseGroupGatherService {
			// TODO
			gatherers: vec![
				DummyGatherer {
					delay_s: 1,
					match_on_substr: "discogs".to_string(),
				},
				DummyGatherer {
					delay_s: 2,
					match_on_substr: "spotify".to_string(),
				},
			],
		})
	}

	pub async fn gather_genres(&self, entity_url: &str) -> anyhow::Result<Vec<String>> {
		let parsed_url = Url::parse(entity_url)?;

		if let Some(gatherer) = self.gatherers.iter().find(|g| g.can_gather(&parsed_url)) {
			let result = gatherer.gather_genres(&parsed_url).await;
			info!("Gathered {result:?} for URL '{entity_url}'.");
			result
		} else {
			debug!("No gatherer found for URL '{entity_url}'.");
			Ok(Vec::new())
		}
	}
}
