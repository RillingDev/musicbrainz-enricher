use reqwest::Url;

pub mod dummy;

pub trait ReleaseGroupGatherer {
	fn can_gather(&self, entity_url: &Url) -> bool;

	async fn gather_genres(&self, entity_url: &Url) -> anyhow::Result<Vec<String>>;
}
