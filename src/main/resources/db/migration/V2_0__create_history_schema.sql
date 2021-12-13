DROP INDEX musicbrainz_enricher.history_entry_unique_index_mbid;

ALTER TABLE musicbrainz_enricher.history_entry
	ADD CONSTRAINT history_entry_data_type_mbid_uq UNIQUE (data_type, mbid);
