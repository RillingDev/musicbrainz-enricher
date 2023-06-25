CREATE OR REPLACE VIEW musicbrainz_enricher.release_work_queue AS
SELECT *
FROM musicbrainz_enricher.release_with_relationships r
		 LEFT JOIN musicbrainz_enricher.release_history_entry rhe ON r.gid = rhe.release_gid
WHERE rhe.release_gid IS NULL;

CREATE OR REPLACE VIEW musicbrainz_enricher.release_group_work_queue AS
SELECT *
FROM musicbrainz_enricher.release_group_with_relationships rg
		 LEFT JOIN musicbrainz_enricher.release_group_history_entry rghe ON rg.gid = rghe.release_group_gid
WHERE rghe.release_group_gid IS NULL;
