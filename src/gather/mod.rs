use std::sync::Arc;

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
use futures::future::join_all;
use log::{info, warn};
use tokio::task::{self};
use tokio_postgres::Client;

mod gatherer;
mod genre_matcher;

pub async fn run_gather(mut db_client: Client) -> anyhow::Result<()> {
	let genre_names = select_genre_names(&db_client).await?;
	let genre_matcher = default_genre_canonical_string_matcher(
		genre_names
			.iter()
			.map(std::string::String::as_str)
			.collect(),
	)?;

	gather_release_groups(&mut db_client, &genre_matcher).await?;
	Ok(())
}

// TODO history saving

const SELECT_CHUNK_SIZE: u32 = 100;

async fn gather_release_groups(
	db_client: &mut Client,
	genre_matcher: &CanonicalStringMatcher,
) -> anyhow::Result<()> {
	// TODO
	let gatherer_service = Arc::new(ReleaseGroupGatherService {
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
	});

	let mut offset: u32 = 0;
	let mut results;

	loop {
		results = select_release_group_urls(db_client, SELECT_CHUNK_SIZE, offset).await?;
		let result_len: u32 = results.len().try_into()?;

		if result_len == 0 {
			break;
		}

		info!("Selected {result_len} entities for gathering.");
		let gather_results =
			do_gather_release_groups(genre_matcher, Arc::clone(&gatherer_service), results).await?;
		info!(
			"Gathered {} results for {result_len} entities.",
			gather_results.len()
		);

		let tx = db_client.transaction().await?;
		insert_release_group_enrichment_results(tx.client(), gather_results).await?;
		tx.commit().await?;

		offset += result_len;
	}

	Ok(())
}

async fn do_gather_release_groups(
	genre_matcher: &CanonicalStringMatcher,
	gatherer_service: Arc<ReleaseGroupGatherService>,
	url_and_release_groups: Vec<UrlAndReleaseGroupId>,
) -> anyhow::Result<Vec<ReleaseGroupEnrichmentResult>> {
	let futures = url_and_release_groups
		.into_iter()
		.map(|url_and_release_group| {
			let gatherer_service = Arc::clone(&gatherer_service);
			task::spawn(async move {
				gatherer_service
					.gather_genres(&url_and_release_group.url)
					.await
					.map(|unmatched_genres| (url_and_release_group, unmatched_genres))
			})
		});

	let join_results = join_all(futures).await;

	let mapped_results: Vec<ReleaseGroupEnrichmentResult> = join_results
		.into_iter()
		.filter_map(|join_result| match join_result {
			Ok(gather_result) => match gather_result {
				Ok(inner) => Some(inner),
				Err(e) => {
					warn!("Gathering failed, ignoring it: {e}");
					None
				}
			},
			Err(e) => {
				warn!("Joining failed, ignoring it: {e}");
				None
			}
		})
		.flat_map(|(url_and_release_group, unmatched_genres)| {
			unmatched_genres
				.into_iter()
				.filter_map(|unmatched_genre| genre_matcher.canonicalize(&unmatched_genre))
				.map(move |genre| ReleaseGroupEnrichmentResult {
					target_mbid: url_and_release_group.target_mbid,
					url: url_and_release_group.url.to_string(),
					genre,
				})
		})
		.collect();

	Ok(mapped_results)
}
