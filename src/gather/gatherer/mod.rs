use log::debug;
use reqwest::Url;

use crate::gather::gatherer::dummy::DummyGatherer;

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
	pub async fn gather_genres(&self, entity_url: &str) -> anyhow::Result<Vec<String>> {
		let parsed_url = Url::parse(entity_url)?;

		if let Some(gatherer) = self.gatherers.iter().find(|g| g.can_gather(&parsed_url)) { gatherer.gather_genres(&parsed_url).await } else {
  				debug!("No gatherer found for URL '{entity_url}'.");
  				Ok(Vec::new())
  			}
	}
}
