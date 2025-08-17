# MusicBrainz Enricher

> A Java application to enrich MusicBrainz data from linked sources.

## About

Queries the [MusicBrainz](https://musicbrainz.org/) API and fetches data from linked sources like Discogs or Spotify.

### Requirements

- Java Runtime Environment 21
- Docker

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

This application uses Spring Boot, which allows for easy handling of configurations.
See [the Spring Boot documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/howto.html#howto.properties-and-configuration.external-properties-location)
for details.

- `musicbrainz-enricher.host` (Either "test.musicbrainz.org" or "musicbrainz.org")
- Credentials
	- Musicbrainz
		- `musicbrainz-enricher.musicbrainz.username`
		- `musicbrainz-enricher.musicbrainz.password`
	- [Discogs API](https://www.discogs.com/developers/)(can be left empty to use (slower) unauthenticated discogs API
	  access)
		- `musicbrainz-enricher.discogs.token`
	- [Spotify API](https://developer.spotify.com/documentation/web-api) (can be left empty to disable spotify API
	  access)
		- `musicbrainz-enricher.spotify.client-id`
		- `musicbrainz-enricher.spotify.client-secret`

## Usage

Before starting, set up a copy of the MusicBrainz database using <https://github.com/metabrainz/musicbrainz-docker>
locally with the database port open.

Auto-query mode will enrich every entity from the MusicBrainz database:

`java -jar musicbrainz-enricher*.jar`.

### History

The application will remember entities checked already and will not re-check them. To reset this, truncate the
`*_history_entry` and `enricher_release_group_result` tables in the `musicbrainz_enricher` schema.
