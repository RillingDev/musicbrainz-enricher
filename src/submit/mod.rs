use itertools::Itertools;
use log::{info, warn};
use reqwest::Url;
use tokio_postgres::Client;

use crate::{
	sql::{ReleaseGroupEnrichmentMergedResult, refresh_merged_results, select_merged_results},
	submit::musicbrainz::{MusicbrainzClient, ReleaseGroupTags, UserTag},
};

pub use crate::submit::musicbrainz::MusicbrainzCredentials;

mod musicbrainz;

const TAG_SUBMISSION_CHUNK_SIZE: u32 = 50;

pub async fn run_submit(
	db_client: Client,
	musicbrainz_url: Url,
	musicbrainz_credentials: MusicbrainzCredentials,
) -> anyhow::Result<()> {
	let mb_client = MusicbrainzClient::new(musicbrainz_url, musicbrainz_credentials)?;

	let mut offset: u32 = 0;
	let mut results;

	info!("Refreshing results...");
	refresh_merged_results(&db_client).await?;
	info!("Refreshed results.");

	loop {
		results = select_merged_results(&db_client, TAG_SUBMISSION_CHUNK_SIZE, offset).await?;
		let result_len: u32 = results.len().try_into()?;

		if result_len == 0 {
			break;
		}

		info!("Selected {result_len} results (offset {offset}) for submission.");
		// TODO: if a single release-group cannot be found, this fails. Maybe check for existence beforehand?
		if let Err(err) = mb_client
			.submit_release_group_tags(results_to_tags(results))
			.await
		{
			warn!("Failed to submit: {err}.");
		}

		offset += result_len;
	}

	Ok(())
}

fn results_to_tags(results: Vec<ReleaseGroupEnrichmentMergedResult>) -> ReleaseGroupTags {
	results
		.into_iter()
		.chunk_by(|r| r.target_mbid)
		.into_iter()
		.map(|(target_mbid, grouped)| {
			(
				target_mbid,
				grouped.map(|r| UserTag::new(r.genre)).collect(),
			)
		})
		.collect()
}
