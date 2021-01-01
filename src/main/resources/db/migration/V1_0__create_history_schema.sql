CREATE TABLE history_entry
(
    id           BIGINT                   NOT NULL AUTO_INCREMENT PRIMARY KEY,
    mbid         UUID                     NOT NULL,
    data_type    INTEGER                  NOT NULL,
    last_checked TIMESTAMP WITH TIME ZONE NOT NULL
);
CREATE UNIQUE INDEX history_entry_unique_index_c ON history_entry (mbid, data_type);
