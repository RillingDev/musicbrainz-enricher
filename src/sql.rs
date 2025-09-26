use anyhow::Context;
use itertools::Itertools;
use tokio_postgres::Client;
use uuid::Uuid;

pub async fn init_schema(db_client: &mut Client) -> anyhow::Result<()> {
	db_client
		.batch_execute(include_str!("schema.sql"))
		.await
		.context("Failed to initialize schema.")
}

#[derive(Debug)]
pub struct ReleaseGroupEnrichmentMergedResult {
	pub target_mbid: Uuid,
	pub genres: Vec<String>,
}

pub async fn select_merged_results(
	db_client: &mut Client,
	limit: u32,
	offset: u32,
) -> anyhow::Result<Vec<ReleaseGroupEnrichmentMergedResult>> {
	let limit: i64 = limit.into();
	let offset: i64 = offset.into();
	// We limit on the distinct ID instead of the rows as we need to submit all data for one ID together.
	let res = db_client.query(
        "SELECT target_release_group_gid, genre_name
        FROM musicbrainz_enricher.release_group_result_merged
        WHERE target_release_group_gid IN (
            SELECT DISTINCT(target_release_group_gid) FROM musicbrainz_enricher.release_group_result_merged
            LIMIT $1 OFFSET $2
        )",
        &[&limit, &offset],
    ).await?;

	Ok(res
		.iter()
		.map(|row| {
			let target_release_group_gid: Uuid = row.get(0);
			let genre_name: String = row.get(1);
			(target_release_group_gid, genre_name)
		})
		.chunk_by(|(target_release_group_gid, _)| *target_release_group_gid)
		.into_iter()
		.map(
			|(target_release_group_gid, grouped)| ReleaseGroupEnrichmentMergedResult {
				target_mbid: target_release_group_gid,
				genres: grouped.map(|(_, genre)| genre).collect(),
			},
		)
		.collect())
}
