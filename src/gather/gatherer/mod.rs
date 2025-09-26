use reqwest::Url;
use std::collections::HashSet;

mod dummy;

pub trait ReleaseGroupGatherer {
	fn gather_genres(&self, entity_url: Url) -> anyhow::Result<HashSet<String>>;
}
