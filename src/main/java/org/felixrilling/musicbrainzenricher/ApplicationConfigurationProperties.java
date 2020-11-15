package org.felixrilling.musicbrainzenricher;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Configuration
@ConfigurationProperties(prefix = "musicbrainz-enricher")
public class ApplicationConfigurationProperties {

    /**
     * Musicbrainz host, e.g. {@code musicbrainz.org} or {@code test.musicbrainz.org}.
     */
    @NotBlank
    private String host;


    /**
     * Application ID/name/
     */
    @NotBlank
    @Pattern(regexp = "^\\w+$")
    private String name;

    /**
     * Application version.
     */
    @NotBlank
    private String version;

    /**
     * Application contact data (mail address or URL).
     */
    @NotBlank
    private String contact;


    /**
     * Musicbrainz credentials.
     * You should probably pass these as command line flags.
     */
    @NotBlank
    private MusicbrainzCredentials musicbrainz;


    /**
     * Spotify credentials.
     * You should probably pass these as command line flags.
     * Blank sub-values disable spotify integration.
     */
    @NotBlank
    private SpotifyCredentials spotify;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public MusicbrainzCredentials getMusicbrainz() {
        return musicbrainz;
    }

    public void setMusicbrainz(MusicbrainzCredentials musicbrainz) {
        this.musicbrainz = musicbrainz;
    }

    public SpotifyCredentials getSpotify() {
        return spotify;
    }

    public void setSpotify(SpotifyCredentials spotify) {
        this.spotify = spotify;
    }

    private static class MusicbrainzCredentials {
        @NotBlank
        private String username;

        @NotBlank
        private String password;

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }

    private static class SpotifyCredentials {


        private String clientId;

        private String clientSecret;

        public String getClientId() {
            return clientId;
        }

        public void setClientId(String clientId) {
            this.clientId = clientId;
        }

        public String getClientSecret() {
            return clientSecret;
        }

        public void setClientSecret(String clientSecret) {
            this.clientSecret = clientSecret;
        }
    }
}