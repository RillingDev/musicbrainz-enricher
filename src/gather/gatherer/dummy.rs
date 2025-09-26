use crate::gather::gatherer::ReleaseGroupGatherer;

pub struct DummyGatherer {}

impl ReleaseGroupGatherer for DummyGatherer {
	fn can_gather(&self, entity_url: &reqwest::Url) -> bool {
		true
	}

	async fn gather_genres(&self, _entity_url: &reqwest::Url) -> anyhow::Result<Vec<String>> {
		Ok(["fizz".to_string(), "rock".to_string()].to_vec())
	}
}
