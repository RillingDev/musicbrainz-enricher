use std::{
	collections::{HashMap, HashSet},
	sync::LazyLock,
	time::Duration,
};

use leaky_bucket::RateLimiter;
use log::warn;
use regex::Regex;
use reqwest::{Client, ClientBuilder, Url, header};
use serde::Deserialize;
use uuid::Uuid;

use crate::http::USER_AGENT;

const GENRE_PROPERTY_ID: &str = "P136";
const MUSICBRAINZ_LINK_PROPERTY_ID: &str = "P8052";

type WikidataItems = HashMap<String, Vec<WikidataStatement>>;

#[derive(Debug, Deserialize)]
struct WikidataStatement {
	id: String,
	value: WikidataStatementValue,
}

#[derive(Debug, Deserialize)]
struct WikidataStatementValue {
	#[serde(rename = "type")]
	statement_type: String,
	content: String,
}

#[derive(Debug)]
pub struct WikidataClient {
	base_url: Url,
	client: Client,
	limiter: RateLimiter,
	genre_mbid_map: HashMap<Uuid, String>,
}

static RELEASE_GROUP_ID_PATTERN: LazyLock<Regex> =
	LazyLock::new(|| Regex::new(".+/(?<id>Q\\d+)$").expect("Regex construction failed."));

// See https://www.wikidata.org/wiki/Wikidata:REST_API
impl WikidataClient {
	pub fn new(genre_mbid_map: HashMap<Uuid, String>) -> Result<Self, anyhow::Error> {
		let base_url = Url::parse("https://www.wikidata.org")?;
		// See https://api.wikimedia.org/wiki/Rate_limits
		// further slowed down to adapt for network fluctuations.
		let limiter = RateLimiter::builder()
			.interval(Duration::from_secs(15))
			.refill(2)
			.initial(2)
			.build();

		let client = ClientBuilder::new().user_agent(USER_AGENT).build()?;

		Ok(WikidataClient {
			base_url,
			client,
			limiter,
			genre_mbid_map,
		})
	}

	async fn get_statements(
		&self,
		entity_id: &str,
		property_id: &str,
	) -> anyhow::Result<WikidataItems> {
		// https://doc.wikimedia.org/Wikibase/master/js/rest-api/#/statements/getItemStatements
		let mut url = self.base_url.clone();
		url.set_path(
			format!("w/rest.php/wikibase/v1/entities/items/{entity_id}/statements").as_str(),
		);
		url.set_query(Some(format!("property={property_id}").as_str()));

		self.limiter.acquire_one().await;

		let items = self
			.client
			.get(url)
			.header(header::ACCEPT, "application/json")
			.send()
			.await?
			.error_for_status()?
			.json::<WikidataItems>()
			.await?;

		Ok(items)
	}

	async fn lookup_release_group(&self, id: &str) -> anyhow::Result<Vec<String>> {
		// e.g. https://www.wikidata.org/w/rest.php/wikibase/v1/entities/items/Q53020187/statements?property=P136
		let wikidata_release_group_items = self.get_statements(id, GENRE_PROPERTY_ID).await?;

		let statements = wikidata_release_group_items
			.get(GENRE_PROPERTY_ID)
			.into_iter()
			.flatten();

		let mut genres = Vec::new();
		for stmt in statements {
			if let Some(gb) = self.lookup_genre(&stmt.value.content).await? {
				genres.push(gb);
			}
		}
		Ok(genres)
	}

	async fn lookup_genre(&self, wikidata_genre_id: &str) -> anyhow::Result<Option<String>> {
		// e.g. https://www.wikidata.org/w/rest.php/wikibase/v1/entities/items/Q968730/statements?property=P8052
		// TODO: this request could be cached as the result rarely changes
		let genre_items = self
			.get_statements(wikidata_genre_id, MUSICBRAINZ_LINK_PROPERTY_ID)
			.await?;

		if let Some(statements) = genre_items.get(MUSICBRAINZ_LINK_PROPERTY_ID) {
			if statements.len() > 1 {
				warn!("Found more than one musicbrainz link for genre {wikidata_genre_id}.");
			}
			if let Some(statement) = statements.first() {
				let musicbrainz_genre_id = Uuid::parse_str(&statement.value.content)?;
				if let Some(musicbrainz_genre_name) = self.genre_mbid_map.get(&musicbrainz_genre_id)
				{
					return Ok(Some(musicbrainz_genre_name.clone()));
				}
			}
		}

		Ok(None)
	}

	pub fn supported_hosts() -> HashSet<String> {
		HashSet::from(["www.wikidata.org".to_string()])
	}

	pub async fn gather_genres(&self, entity_url: &Url) -> anyhow::Result<Vec<String>> {
		if let Some(captures) = RELEASE_GROUP_ID_PATTERN.captures(entity_url.path()) {
			let id = &captures["id"];

			let genres = self.lookup_release_group(id).await?;
			Ok(genres)
		} else {
			warn!("Could not extract ID from URL '{entity_url}'.");
			Ok(Vec::new())
		}
	}
}

#[cfg(test)]
mod tests {
	use super::*;

	#[test]
	fn model_deserializes() -> anyhow::Result<()> {
		// https://www.wikidata.org/w/rest.php/wikibase/v1/entities/items/Q53020187/statements?property=P136
		let json = r#"{"P136":[{"id":"Q53020187$E948DC29-2752-4251-848A-52CF8AC2CDDF","rank":"normal","qualifiers":[],"references":[{"hash":"0b3d2be1b2f7e61302422274af98f733f3e50349","parts":[{"property":{"id":"P143","data_type":"wikibase-item"},"value":{"type":"value","content":"Q328"}},{"property":{"id":"P4656","data_type":"url"},"value":{"type":"value","content":"https:\/\/en.wikipedia.org\/w\/index.php?title=Bad_Witch&oldid=869873058"}}]}],"property":{"id":"P136","data_type":"wikibase-item"},"value":{"type":"value","content":"Q968730"}}]}"#;

		let items = serde_json::from_str::<WikidataItems>(json)?;

		assert_eq!(items.len(), 1);

		Ok(())
	}

	#[tokio::test]
	#[ignore]
	async fn get_statements_manual_test() -> anyhow::Result<()> {
		let client = WikidataClient::new(HashMap::new()).await?;

		let result = client.get_statements("Q53020187", "P136").await;
		println!("Result is {result:?}");
		assert!(result.is_ok());

		Ok(())
	}

	#[tokio::test]
	#[ignore]
	async fn gather_genres_manual_test() -> anyhow::Result<()> {
		todo!()
	}
}
