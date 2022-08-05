CREATE TABLE musicbrainz_enricher.history_entry
(
	id        BIGINT  NOT NULL GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
	mbid      UUID    NOT NULL, /* Note that this may only be unique for each data type, not globally */
	data_type INTEGER NOT NULL, /* Mapped to e.g. release, release-group, etc... */
	CONSTRAINT history_entry_data_type_mbid_uq UNIQUE (data_type, mbid)
);
