CREATE SCHEMA IF NOT EXISTS musicbrainz_enricher;
--
CREATE TABLE IF NOT EXISTS musicbrainz_enricher.release_group_result (
-- Until we switch to using lookups against the DB instead of the API, this is not hard reference
target_release_group_gid uuid NOT NULL
/*REFERENCES musicbrainz.release_group (gid) ON DELETE CASCADE*/
,
-- This implicitly references musicbrainz.url. As its only used for debugging, no constraint is used.
source_url VARCHAR NOT NULL,
-- This implicitly references musicbrainz.genre. To make submission easier it is denormalized here
genre_name VARCHAR NOT NULL
);
CREATE OR REPLACE VIEW musicbrainz_enricher.release_group_result_merged AS WITH genre_counts AS (
        SELECT target_release_group_gid,
            genre_name,
            RANK() OVER (
                PARTITION BY target_release_group_gid
                ORDER BY COUNT(*) DESC
            ) AS genre_rank
        FROM musicbrainz_enricher.release_group_result
        GROUP BY target_release_group_gid,
            genre_name
    )
SELECT target_release_group_gid,
    genre_name
FROM genre_counts
WHERE genre_rank = 1
ORDER BY target_release_group_gid;