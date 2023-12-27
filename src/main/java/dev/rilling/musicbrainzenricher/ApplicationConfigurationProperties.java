package dev.rilling.musicbrainzenricher;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

@Configuration
@ConfigurationProperties(prefix = "musicbrainz-enricher")
@Validated
public class ApplicationConfigurationProperties {

	/**
	 * Musicbrainz host, e.g. {@code musicbrainz.org} or {@code test.musicbrainz.org}.
	 */
	@NotBlank
	private String host;


	/**
	 * Application ID/name.
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
	 * If changes should just be logged and not applied.
	 */
	private boolean dryRun;

	/**
	 * Size of the thread pool to use for enrichment.
	 */
	private int enrichmentThreadPoolSize;

	/**
	 * Musicbrainz credentials.
	 * You should probably pass these as command line flags.
	 */
	private MusicbrainzCredentials musicbrainz;


	/**
	 * Discogs credentials.
	 * You should probably pass these as command line flags.
	 * Blank sub-values disable authenticated discogs access.
	 */
	private DiscogsCredentials discogs;

	/**
	 * Spotify credentials.
	 * You should probably pass these as command line flags.
	 * Blank sub-values disable spotify integration.
	 */
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

	public boolean isDryRun() {
		return dryRun;
	}

	public void setDryRun(boolean dryRun) {
		this.dryRun = dryRun;
	}

	public int getEnrichmentThreadPoolSize() {
		return enrichmentThreadPoolSize;
	}

	public void setEnrichmentThreadPoolSize(int enrichmentThreadPoolSize) {
		this.enrichmentThreadPoolSize = enrichmentThreadPoolSize;
	}

	public MusicbrainzCredentials getMusicbrainz() {
		return musicbrainz;
	}

	public void setMusicbrainz(MusicbrainzCredentials musicbrainz) {
		this.musicbrainz = musicbrainz;
	}

	public DiscogsCredentials getDiscogs() {
		return discogs;
	}

	public void setDiscogs(DiscogsCredentials discogs) {
		this.discogs = discogs;
	}

	public SpotifyCredentials getSpotify() {
		return spotify;
	}

	public void setSpotify(SpotifyCredentials spotify) {
		this.spotify = spotify;
	}

	public record MusicbrainzCredentials(@NotBlank String username, @NotBlank String password) {
	}

	// https://www.discogs.com/developers/#page:authentication
	public record DiscogsCredentials(String token) {
	}

	public record SpotifyCredentials(String clientId, String clientSecret) {
	}
}
