use crate::{
	gather::{
		gatherer::{ReleaseGroupGatherService, dummy::DummyGatherer},
		genre_matcher::{CanonicalStringMatcher, default_genre_canonical_string_matcher},
	},
	sql::{
		ReleaseGroupEnrichmentResult, UrlAndReleaseGroupId,
		insert_release_group_enrichment_results, select_genre_names, select_release_group_urls,
	},
};
use log::info;
use tokio_postgres::Client;

mod gatherer;
mod genre_matcher;

pub async fn run_gather(db_client: Client) -> anyhow::Result<()> {
	let genre_names = select_genre_names(&db_client).await?;
	let genre_matcher = default_genre_canonical_string_matcher(
		genre_names
			.iter()
			.map(std::string::String::as_str)
			.collect(),
	)?;

	gather_release_groups(&db_client, genre_matcher).await?;
	Ok(())
}

const SELECT_CHUNK_SIZE: u32 = 100;

async fn gather_release_groups(
	db_client: &Client,
	genre_matcher: CanonicalStringMatcher,
) -> anyhow::Result<()> {
	// TODO
	let gatherer_service = ReleaseGroupGatherService {
		genre_matcher,
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
	};

	let mut offset: u32 = 0;
	let mut results;

	loop {
		results = select_release_group_urls(db_client, SELECT_CHUNK_SIZE, offset).await?;
		let result_len: u32 = results.len().try_into()?;

		if result_len == 0 {
			break;
		}

		info!("Selected {result_len} entities for gathering.");
		let gather_results = do_gather_release_groups(&gatherer_service, results).await?;
		info!(
			"Gathered {} results for {result_len} entities.",
			gather_results.len()
		);
		insert_release_group_enrichment_results(db_client, gather_results).await?;

		offset += result_len;
	}

	Ok(())
}

async fn do_gather_release_groups(
	gatherer_service: &ReleaseGroupGatherService,
	items: Vec<UrlAndReleaseGroupId>,
) -> anyhow::Result<Vec<ReleaseGroupEnrichmentResult>> {
	// TODO: add concurrency
	let mut res = Vec::new();
	for item in items {
		let genres = gatherer_service.gather_genres(&item.url).await?;
		let mut gatherer_res = genres
			.iter()
			.map(|genre| ReleaseGroupEnrichmentResult {
				target_mbid: item.target_mbid,
				url: item.url.clone(),
				genre: genre.clone(),
			})
			.collect();
		res.append(&mut gatherer_res);
	}
	Ok(res)
}
