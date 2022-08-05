CREATE TABLE musicbrainz_enricher.release_history_entry
(
	id          BIGINT      NOT NULL GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
	release_gid UUID UNIQUE NOT NULL,
	CONSTRAINT release_history_entry_release_fk FOREIGN KEY (release_gid) REFERENCES musicbrainz.release (gid)
		ON DELETE CASCADE
);

CREATE TABLE musicbrainz_enricher.release_group_history_entry
(
	id                BIGINT      NOT NULL GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
	release_group_gid UUID UNIQUE NOT NULL,
	CONSTRAINT release_group_history_entry_release_group_fk FOREIGN KEY (release_group_gid) REFERENCES musicbrainz.release_group (gid)
		ON DELETE CASCADE
);
