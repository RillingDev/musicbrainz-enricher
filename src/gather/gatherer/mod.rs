use anyhow::Ok;
use log::{debug, info};
use reqwest::Url;
use std::collections::{HashMap, HashSet};
use uuid::Uuid;

use crate::gather::gatherer::{
	discogs::{DiscogsClient, DiscogsCredentials},
	wikidata::WikidataClient,
};

pub mod discogs;
pub mod wikidata;

#[derive(Debug)]
pub enum ReleaseGroupGatherer {
	Discogs(DiscogsClient),
	Wikidata(WikidataClient),
}

impl ReleaseGroupGatherer {
	fn supported_hosts(&self) -> HashSet<String> {
		match self {
			ReleaseGroupGatherer::Discogs(_) => DiscogsClient::supported_hosts(),
			ReleaseGroupGatherer::Wikidata(_) => WikidataClient::supported_hosts(),
		}
	}

	async fn gather_genres(&self, entity_url: &Url) -> anyhow::Result<Vec<String>> {
		match self {
			ReleaseGroupGatherer::Discogs(discogs_client) => {
				discogs_client.gather_genres(entity_url).await
			}
			ReleaseGroupGatherer::Wikidata(wikidata_client) => {
				wikidata_client.gather_genres(entity_url).await
			}
		}
	}
}

#[derive(Debug)]
pub struct ReleaseGroupGatherService {
	gatherers: Vec<ReleaseGroupGatherer>,
}

impl ReleaseGroupGatherService {
	pub fn new(
		known_genres: HashMap<Uuid, String>,
		discogs_credentials: Option<DiscogsCredentials>,
	) -> anyhow::Result<Self> {
		let discogs_client = DiscogsClient::new(discogs_credentials)?;
		let wikidata_client = WikidataClient::new(known_genres)?;

		Ok(ReleaseGroupGatherService {
			gatherers: vec![
				ReleaseGroupGatherer::Discogs(discogs_client),
				ReleaseGroupGatherer::Wikidata(wikidata_client),
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
