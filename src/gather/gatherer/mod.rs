use log::debug;
use reqwest::Url;

use crate::gather::{gatherer::dummy::DummyGatherer, genre_matcher::CanonicalStringMatcher};

pub mod dummy;

pub trait ReleaseGroupGatherer {
	fn can_gather(&self, entity_url: &Url) -> bool;

	async fn gather_genres(&self, entity_url: &Url) -> anyhow::Result<Vec<String>>;
}
pub struct ReleaseGroupGatherService {
	// TODO
	pub genre_matcher: CanonicalStringMatcher,
	pub gatherers: Vec<DummyGatherer>,
}

impl ReleaseGroupGatherService {
	pub async fn gather_genres(&self, entity_url: &str) -> anyhow::Result<Vec<String>> {
		let parsed_url = Url::parse(entity_url)?;

		if let Some(gatherer) = self.gatherers.iter().find(|g| g.can_gather(&parsed_url)) {
			let unmatched_genres = gatherer.gather_genres(&parsed_url).await?;
			let matched_genres = unmatched_genres
				.iter()
				.filter_map(|unmatched_genre| self.genre_matcher.canonicalize(unmatched_genre))
				.collect();
			Ok(matched_genres)
		} else {
			debug!("No gatherer found for URL '{entity_url}'.");
			Ok(Vec::new())
		}
	}
}
