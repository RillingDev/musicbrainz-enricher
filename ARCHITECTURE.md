# Architecture

## Persistence

The local copy of the musicbrainz database is running in a PostgreSQL server managed by docker. Data on
the `musicbrainz` schema in this database is accessed read-only via raw JDBC connections.

This application piggybacks onto this database and stores its data in the schema `musicbrainz_enricher`. Data in this
schema is managed via JPA. The DDL is not managed by Hibernate, but rather initialized via raw SQL, and migrated by
Flyway if needed.
