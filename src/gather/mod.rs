use log::{debug, info};
use reqwest::Url;
use tokio_postgres::Client;

use crate::{
	gather::{
		gatherer::{ReleaseGroupGatherer, dummy::DummyGatherer},
		genre_matcher::*,
	},
	sql::{
		ReleaseGroupEnrichmentResult, UrlAndReleaseGroupId,
		insert_release_group_enrichment_results, select_genre_names, select_release_group_urls,
	},
};

mod gatherer;
mod genre_matcher;

pub async fn run_gather(db_client: Client) -> anyhow::Result<()> {
	let genre_names = select_genre_names(&db_client).await?;
	let genre_matcher =
		default_genre_canonical_string_matcher(genre_names.iter().map(|g| g.as_str()).collect())?;

	gather_release_groups(&db_client, &genre_matcher).await?;
	Ok(())
}

const SELECT_CHUNK_SIZE: u32 = 100;

async fn gather_release_groups(
	db_client: &Client,
	genre_matcher: &CanonicalStringMatcher,
) -> anyhow::Result<()> {
	// TODO
	let gatherers = vec![DummyGatherer {}];

	let mut offset: u32 = 0;
	let mut results;

	loop {
		results = select_release_group_urls(db_client, SELECT_CHUNK_SIZE, offset).await?;
		let result_len: u32 = results.len().try_into()?;

		if result_len == 0 {
			break;
		}

		info!("Selected {result_len} entities for gathering.");
		let gather_results = do_gather_release_groups(genre_matcher, &gatherers, results).await?;
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
	genre_matcher: &CanonicalStringMatcher,
	gatherers: &Vec<DummyGatherer>,
	url_and_release_groups: Vec<UrlAndReleaseGroupId>,
) -> anyhow::Result<Vec<ReleaseGroupEnrichmentResult>> {
	let mut res = Vec::new();
	// TODO: add concurrency
	for ele in url_and_release_groups {
		let url = Url::parse(&ele.url)?;
		if let Some(gatherer) = gatherers.iter().find(|g| g.can_gather(&url)) {
			let mut gatherer_res = do_gather_release_group(genre_matcher, gatherer, ele).await?;
			res.append(&mut gatherer_res);
		} else {
			debug!("No gatherer found for url '{url}'.");
		}
	}
	Ok(res)
}

async fn do_gather_release_group(
	genre_matcher: &CanonicalStringMatcher,
	gatherer: &DummyGatherer,
	url_and_release_group: UrlAndReleaseGroupId,
) -> anyhow::Result<Vec<ReleaseGroupEnrichmentResult>> {
	let unmatched_genres: Vec<String> = gatherer
		.gather_genres(&Url::parse(&url_and_release_group.url)?)
		.await?;
	let results = unmatched_genres
		.iter()
		.filter_map(|unmatched_genre| genre_matcher.canonicalize(unmatched_genre))
		.map(|genre| ReleaseGroupEnrichmentResult {
			target_mbid: url_and_release_group.target_mbid,
			url: url_and_release_group.url.clone(),
			genre: genre.clone(),
		})
		.collect();
	Ok(results)
}
