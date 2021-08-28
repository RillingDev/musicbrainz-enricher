# musicbrainz-enricher

> Bot to enrich musicbrainz data from linked sources.

## About

Queries musicbrainz' API and fetches data from linked sources like Discogs or Spotify.

### Supported Relationship Sources

- Releases (mode = `release`)
    - Apple Music / iTunes
    - Bandcamp
    - Discogs
    - Junodownload
    - Spotify
- Release Groups (mode = `release-group`)
    - Allmusic
    - Discogs
    - Wikidata

## Configuration

This application uses Spring Boot which allows for easy handling of configurations. The following
can either be passed as command line arguments (e.g. `--musicbrainz-enricher.host=foo`), or in a
file called `application.properties` in the current working directory (e.g.
containing `musicbrainz-enricher.host=foo`).

- `musicbrainz-enricher.host` (Either "test.musicbrainz.org" or "musicbrainz.org")
- Credentials
    - Musicbrainz
        - `musicbrainz-enricher.musicbrainz.username`
        - `musicbrainz-enricher.musicbrainz.password`
    - Discogs API (can be left empty to use (slower) unauthenticated discogs API access)
        - `musicbrainz-enricher.discogs.token`
    - Spotify API (can be left empty to disable spotify API access)
        - `musicbrainz-enricher.spotify.client-id`
        - `musicbrainz-enricher.spotify.client-secret`

## Usage

Before starting, set up <https://github.com/metabrainz/musicbrainz-docker> locally with the database
port open.

This tool can run in auto-query or single mode. Auto-query mode will enrich every entity from the
musicbrainz database. Single mode takes a musicbrainz MBID and will enrich the matching entity.

Auto-query mode:
`java -jar musicbrainz-enricher*.jar 'release'`.

Single mode:
`java -jar musicbrainz-enricher*.jar 'release' 'MBID'`.

### Local History DB

This application will remember entities checked already and will only re-check them after
duration `n` days, where `n` defaults to 90 days. In order to reset this, truncate the
table `history_entry` in the schema `musicbrainz_enricher`.
