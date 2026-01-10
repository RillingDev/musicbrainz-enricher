# MusicBrainz Enricher

> An application to enrich MusicBrainz data from linked sources.

## About

Queries the [MusicBrainz](https://musicbrainz.org/) API and fetches data from linked sources like Discogs.

**You are looking at the ongoing Rust rewrite of this project. Please find the older version for Java on the `main` branch.**

### Requirements

- Rust
- Docker
- A MusicBrainz Account for submission via their API

### Supported Relationship Sources

- Release Groups
    - Discogs
    - Wikidata

## Usage

Before starting, set up a copy of the MusicBrainz database using <https://github.com/metabrainz/musicbrainz-docker> locally with the database port exposed to localhost.

### Gathering

The first step is gathering data:

`musicbrainz_enricher gather`.

This will go through every relevant relationship and gather data from the referenced URLs. The results are persisted
locally and are not submitted yet.

Note that you may want to add additional arguments for authentication with other APIs, such as Discogs. See `musicbrainz_enricher gather --help` for details.

### Submission

After gathering is complete, you can submit the merged data that was collected to MusicBrainz:

`musicbrainz_enricher submit --musicbrainz-url https://test.musicbrainz.org -u <MusicBrainzUsername> -p <MusicBrainzPassword>`.
