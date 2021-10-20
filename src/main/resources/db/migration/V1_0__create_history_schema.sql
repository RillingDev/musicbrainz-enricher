CREATE TABLE musicbrainz_enricher.history_entry
(
	id           BIGINT                   NOT NULL GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
	mbid         UUID                     NOT NULL, /* Note that this may only be unique for each data type, not globally */
	data_type    INTEGER                  NOT NULL, /* Mapped to e.g. release, release-group, etc... */
	last_checked TIMESTAMP WITH TIME ZONE NOT NULL
);
CREATE UNIQUE INDEX history_entry_unique_index_mbid ON musicbrainz_enricher.history_entry (mbid, data_type);
