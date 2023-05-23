CREATE TABLE musicbrainz_enricher.release_history_entry
(
	release_gid UUID PRIMARY KEY NOT NULL REFERENCES musicbrainz.release (gid)
		ON DELETE CASCADE
);

CREATE MATERIALIZED VIEW musicbrainz_enricher.release_with_relationships AS
SELECT *
FROM musicbrainz.release r
WHERE r.id IN
	  (SELECT lru.entity0 FROM musicbrainz.l_release_url lru);

CREATE VIEW musicbrainz_enricher.release_work_queue AS
SELECT *
FROM musicbrainz_enricher.release_with_relationships r
WHERE r.gid NOT IN (SELECT rhe.release_gid FROM musicbrainz_enricher.release_history_entry rhe);


CREATE TABLE musicbrainz_enricher.release_group_history_entry
(
	release_group_gid UUID PRIMARY KEY NOT NULL REFERENCES musicbrainz.release_group (gid)
		ON DELETE CASCADE
);

CREATE MATERIALIZED VIEW musicbrainz_enricher.release_group_with_relationships AS
SELECT *
FROM musicbrainz.release_group rg
WHERE rg.id IN
	  (SELECT lrgu.entity0 FROM musicbrainz.l_release_group_url lrgu);

CREATE VIEW musicbrainz_enricher.release_group_work_queue AS
SELECT *
FROM musicbrainz_enricher.release_group_with_relationships rg
WHERE rg.gid NOT IN (SELECT rghe.release_group_gid FROM musicbrainz_enricher.release_group_history_entry rghe);
