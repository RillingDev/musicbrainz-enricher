use std::time::Duration;

use reqwest::Url;

use crate::gather::gatherer::ReleaseGroupGatherer;

pub struct DummyGatherer {
	pub delay_s: u64,
	pub match_on_substr: String,
}

impl ReleaseGroupGatherer for DummyGatherer {
	fn can_gather(&self, entity_url: &Url) -> bool {
		entity_url.to_string().contains(&self.match_on_substr)
	}

	async fn gather_genres(&self, _entity_url: &Url) -> anyhow::Result<Vec<String>> {
		let () = tokio::time::sleep(Duration::from_secs(self.delay_s)).await;
		Ok(["fizz".to_string(), "rock".to_string()].to_vec())
	}
}
