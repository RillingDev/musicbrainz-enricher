CREATE SCHEMA IF NOT EXISTS musicbrainz_enricher;

--

CREATE TABLE IF NOT EXISTS musicbrainz_enricher.release_history_entry
(
	release_gid uuid PRIMARY KEY NOT NULL REFERENCES musicbrainz.release (gid)
		ON DELETE CASCADE
);

CREATE MATERIALIZED VIEW IF NOT EXISTS musicbrainz_enricher.release_with_relationships AS
SELECT *
FROM musicbrainz.release r
WHERE r.id IN
	  (SELECT lru.entity0 FROM musicbrainz.l_release_url lru);

CREATE OR REPLACE VIEW musicbrainz_enricher.release_work_queue AS
SELECT *
FROM musicbrainz_enricher.release_with_relationships r
		 LEFT JOIN musicbrainz_enricher.release_history_entry rhe ON r.gid = rhe.release_gid
WHERE rhe.release_gid IS NULL;

--

CREATE TABLE IF NOT EXISTS musicbrainz_enricher.release_group_history_entry
(
	release_group_gid uuid PRIMARY KEY NOT NULL REFERENCES musicbrainz.release_group (gid)
		ON DELETE CASCADE
);

CREATE MATERIALIZED VIEW IF NOT EXISTS musicbrainz_enricher.release_group_with_relationships AS
SELECT *
FROM musicbrainz.release_group rg
WHERE rg.id IN
	  (SELECT lrgu.entity0 FROM musicbrainz.l_release_group_url lrgu);

CREATE OR REPLACE VIEW musicbrainz_enricher.release_group_work_queue AS
SELECT *
FROM musicbrainz_enricher.release_group_with_relationships rg
		 LEFT JOIN musicbrainz_enricher.release_group_history_entry rghe ON rg.gid = rghe.release_group_gid
WHERE rghe.release_group_gid IS NULL;

--


CREATE TABLE IF NOT EXISTS musicbrainz_enricher.enricher_release_group_result
(
	-- Until we switch to using lookups against the DB instead of the API, this is not hard reference
	target_release_group_gid uuid NOT NULL /*REFERENCES musicbrainz.release_group (gid) ON DELETE CASCADE*/,
	-- This implicitly references musicbrainz.url. As its only used for debugging, no constraint is used.
	source_url               VARCHAR NOT NULL,
	-- This implicitly references musicbrainz.genre. To make submission easier it is denormalized here
	genre_name               VARCHAR NOT NULL
);

CREATE OR REPLACE VIEW musicbrainz_enricher.enricher_release_group_result_merged AS
WITH genre_counts AS (SELECT target_release_group_gid,
							 genre_name,
							 RANK() OVER (
								 PARTITION BY target_release_group_gid
								 ORDER BY COUNT(*) DESC
								 ) AS genre_rank
					  FROM musicbrainz_enricher.enricher_release_group_result
					  GROUP BY target_release_group_gid, genre_name)
SELECT target_release_group_gid, genre_name
FROM genre_counts
WHERE genre_rank = 1
ORDER BY target_release_group_gid;
