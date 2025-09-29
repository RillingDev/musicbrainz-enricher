use log::info;
use reqwest::Url;
use tokio_postgres::Client;

use crate::{sql::select_merged_results, submit::musicbrainz::MusicbrainzClient};

pub use crate::submit::musicbrainz::MusicbrainzCredentials;

mod musicbrainz;

const TAG_SUBMISSION_CHUNK_SIZE: u32 = 25;

pub async fn run_submit(
	db_client: Client,
	musicbrainz_url: Url,
	musicbrainz_credentials: MusicbrainzCredentials,
) -> anyhow::Result<()> {
	let mb_client = MusicbrainzClient::new(musicbrainz_url, musicbrainz_credentials)?;

	let mut offset: u32 = 0;
	let mut results;

	loop {
		results = select_merged_results(&db_client, TAG_SUBMISSION_CHUNK_SIZE, offset).await?;
		let result_len: u32 = results.len().try_into()?;

		if result_len == 0 {
			break;
		}

		info!("Selected {result_len} results for submission.");
		mb_client.submit_tags(results).await?;

		offset += result_len;
	}

	Ok(())
}
