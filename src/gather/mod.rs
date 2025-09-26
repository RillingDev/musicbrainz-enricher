use log::info;
use tokio_postgres::Client;

use crate::sql::{
	ReleaseGroupEnrichmentResult, UrlAndReleaseGroupId, insert_release_group_enrichment_result,
	select_release_group_urls,
};

mod gatherer;
mod genre_matcher;

pub async fn run_gather(db_client: Client) -> anyhow::Result<()> {
	gather_release_groups(&db_client).await?;
	Ok(())
}

const SELECT_CHUNK_SIZE: u32 = 100;

async fn gather_release_groups(db_client: &Client) -> anyhow::Result<()> {
	let mut offset: u32 = 0;
	let mut results;

	loop {
		results = select_release_group_urls(db_client, SELECT_CHUNK_SIZE, offset).await?;
		let result_len: u32 = results.len().try_into()?;

		if result_len == 0 {
			break;
		}

		info!("Selected {result_len} entities for gathering.");
		let gather_results = do_gather_release_groups(results).await?;
		info!(
			"Gathered {} results for {result_len} entities.",
			gather_results.len()
		);
		insert_release_group_enrichment_result(db_client, gather_results).await?;

		offset += result_len;
	}

	Ok(())
}

async fn do_gather_release_groups(
	release_groups: Vec<UrlAndReleaseGroupId>,
) -> anyhow::Result<Vec<ReleaseGroupEnrichmentResult>> {
	todo!()
}
