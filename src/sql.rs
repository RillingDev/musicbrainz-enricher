use std::collections::HashMap;

use anyhow::Context;
use tokio_postgres::Client;
use uuid::Uuid;

pub async fn init_schema(db_client: &Client) -> anyhow::Result<()> {
	db_client
		.batch_execute(include_str!("schema.sql"))
		.await
		.context("Failed to initialize schema.")
}

pub async fn select_genres(db_client: &Client) -> anyhow::Result<HashMap<Uuid, String>> {
	let rows = db_client
		.query("SELECT gid, name FROM musicbrainz.genre", &[])
		.await?;

	Ok(rows
		.iter()
		.map(|row| {
			let gid: Uuid = row.get(0);
			let name: String = row.get(1);
			(gid, name)
		})
		.collect())
}

#[derive(Debug)]
pub struct UrlAndReleaseGroupId {
	pub url: String,
	pub target_mbid: Uuid,
}

pub async fn select_release_group_urls(
	db_client: &Client,
	limit: u32,
	offset: u32,
) -> anyhow::Result<Vec<UrlAndReleaseGroupId>> {
	let limit: i64 = limit.into();
	let offset: i64 = offset.into();
	let rows = db_client
		.query(
			"SELECT url, release_group_gid
        FROM musicbrainz_enricher.release_group_url
        LIMIT $1 OFFSET $2",
			&[&limit, &offset],
		)
		.await?;

	Ok(rows
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
pub struct ReleaseGroupEnrichmentResult {
	pub genre: String,
	pub url: String,
	pub target_mbid: Uuid,
}

// Should be called in a transaction
pub async fn insert_release_group_enrichment_results(
	db_client: &Client,
	results: Vec<ReleaseGroupEnrichmentResult>,
) -> anyhow::Result<()> {
	let statement = db_client
		.prepare(
			"INSERT INTO
			musicbrainz_enricher.release_group_result (target_release_group_gid, source_url, genre_name)
			VALUES ($1, $2, $3)",
		)
		.await?;
	for result in results {
		db_client
			.execute(
				&statement,
				&[&result.target_mbid, &result.url, &result.genre],
			)
			.await?;
	}
	Ok(())
}

#[derive(Debug)]
pub struct ReleaseGroupEnrichmentMergedResult {
	pub target_mbid: Uuid,
	pub genre: String,
}

pub async fn refresh_merged_results(db_client: &Client) -> anyhow::Result<()> {
	db_client
		.execute(
			"REFRESH MATERIALIZED VIEW musicbrainz_enricher.release_group_result_merged;",
			&[],
		)
		.await?;

	Ok(())
}

pub async fn select_merged_results(
	db_client: &Client,
	limit: u32,
	offset: u32,
) -> anyhow::Result<Vec<ReleaseGroupEnrichmentMergedResult>> {
	let limit: i64 = limit.into();
	let offset: i64 = offset.into();
	// We limit on the distinct ID instead of the rows as we need to submit all data for one ID together.
	let rows = db_client.query(
        "SELECT target_release_group_gid, genre_name
        FROM musicbrainz_enricher.release_group_result_merged
        WHERE target_release_group_gid IN (
            SELECT DISTINCT(target_release_group_gid) FROM musicbrainz_enricher.release_group_result_merged
            LIMIT $1 OFFSET $2
        )",
        &[&limit, &offset],
    ).await?;

	Ok(rows
		.iter()
		.map(|row| {
			let target_release_group_gid: Uuid = row.get(0);
			let genre_name: String = row.get(1);
			ReleaseGroupEnrichmentMergedResult {
				target_mbid: target_release_group_gid,
				genre: genre_name,
			}
		})
		.collect())
}
