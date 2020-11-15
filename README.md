# musicbrainz-enricher

> Bot to enrich musicbrainz data from linked sources.

## Config

The following values should be passed as startup flags (e.g. `-Dmusicbrainz-enricher.musicbrainz.username=myUserName`)

- `musicbrainz-enricher.host` (Either test.musicbrainz.org or musicbrainz.org)
- Credentials
    - Musicbrainz
        - `musicbrainz-enricher.musicbrainz.username`
        - `musicbrainz-enricher.musicbrainz.password`
    - Spotify (can be left empty to disable spotify)
        - `musicbrainz-enricher.spotify.client-id`
        - `musicbrainz-enricher.spotify.client-secret`