use anyhow::Context;
use itertools::Itertools;
use tokio_postgres::Client;
use uuid::Uuid;

pub async fn init_schema(db_client: &Client) -> anyhow::Result<()> {
	db_client
		.batch_execute(include_str!("schema.sql"))
		.await
		.context("Failed to initialize schema.")
}

// TODO: add newtype for release group ID

#[derive(Debug)]
pub struct UrlAndReleaseGroupId {
	pub url: String,
	pub target_mbid: Uuid,
}

// TODO: allow gatherers to inject regex pattern to match directly in DB
pub async fn select_release_group_urls(
	db_client: &Client,
	limit: u32,
	offset: u32,
) -> anyhow::Result<Vec<UrlAndReleaseGroupId>> {
	let limit: i64 = limit.into();
	let offset: i64 = offset.into();
	let res = db_client
		.query(
			"SELECT url, release_group_gid
        FROM musicbrainz_enricher.release_group_url
        LIMIT $1 OFFSET $2",
			&[&limit, &offset],
		)
		.await?;

	Ok(res
		.iter()
		.map(|row| {
			let url: String = row.get(0);
			let release_group_gid: Uuid = row.get(1);
			UrlAndReleaseGroupId {
				url,
				target_mbid: release_group_gid,
			}
		})
		.collect())
}

#[derive(Debug)]
pub struct ReleaseGroupEnrichmentResult {}

pub async fn insert_release_group_enrichment_result(
	db_client: &Client,
	vec: Vec<ReleaseGroupEnrichmentResult>,
) -> anyhow::Result<()> {
	todo!()
}

#[derive(Debug)]
pub struct ReleaseGroupEnrichmentMergedResult {
	pub target_mbid: Uuid,
	pub genres: Vec<String>,
}

// TODO move out grouping?
pub async fn select_merged_results(
	db_client: &Client,
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
