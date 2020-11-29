# musicbrainz-enricher

> Bot to enrich musicbrainz data from linked sources.

## About

Queries musicbrainz' API and fetches data from linked source like Discogs or Spotify.

### Supported Relationship Sources

- Releases (mode = `release`)
    - Discogs
    - Spotify
    - Bandcamp
    - Apple Music / iTunes
    - Junodownload
- Release Groups (mode = `release-group`)
    - Discogs
    - Allmusic

## Configuration

The following values should be passed as startup flags (e.g. `-Dmusicbrainz-enricher.musicbrainz.username=myUserName`)

- `musicbrainz-enricher.host` (Either test.musicbrainz.org or musicbrainz.org)
- Credentials
    - Musicbrainz
        - `musicbrainz-enricher.musicbrainz.username`
        - `musicbrainz-enricher.musicbrainz.password`
    - Spotify (can be left empty to disable spotify)
        - `musicbrainz-enricher.spotify.client-id`
        - `musicbrainz-enricher.spotify.client-secret`
        
## Usage

Before starting, set up <https://github.com/metabrainz/musicbrainz-docker> locally with the database port open.

This tool can run in full or query mode. Full mode will enrich every entity from the musicbrainz database. Query mode takes a musicbrainz search query and will enrich the results.

Full mode: 
`java [startup flags] -jar musicbrainz-enricher*.jar 'release'`.

Query mode: 
`java [startup flags] -jar musicbrainz-enricher*.jar 'release' 'searchQuery'`.

### Local History DB

This application will remember entities checked already and will only re-check them after duration `n` days, where `n` defaults to 90 days. In order to reset this, delete the musicbrainz-enricher database in `~/.cache/`.
