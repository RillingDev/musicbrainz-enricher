CREATE TABLE musicbrainz_enricher.release_history_entry
(
	release_gid UUID PRIMARY KEY NOT NULL REFERENCES musicbrainz.release (gid)
		ON DELETE CASCADE
);

CREATE TABLE musicbrainz_enricher.release_group_history_entry
(
	release_group_gid UUID PRIMARY KEY NOT NULL REFERENCES musicbrainz.release_group (gid)
		ON DELETE CASCADE
);
