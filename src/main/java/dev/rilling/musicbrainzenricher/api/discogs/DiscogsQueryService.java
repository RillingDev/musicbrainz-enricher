package dev.rilling.musicbrainzenricher.api.discogs;

import io.github.bucket4j.Bucket;
import net.jcip.annotations.ThreadSafe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Map;
import java.util.Optional;

// https://www.discogs.com/developers/
@Service
@ThreadSafe
public class DiscogsQueryService {

	private static final Logger LOGGER = LoggerFactory.getLogger(DiscogsQueryService.class);

	private final Bucket bucket;
	private final RestClient restClient;

	DiscogsQueryService(@Qualifier("discogsRestClient") RestClient restClient,
						@Qualifier("discogsBucket") Bucket bucket) {
		this.bucket = bucket;
		this.restClient = restClient;
	}


	public Optional<DiscogsRelease> lookUpRelease(final String id) {
		bucket.asBlocking().consumeUninterruptibly(1);

		try {
			return Optional.ofNullable(restClient.get().uri("/releases/{id}", Map.of("id", id)).accept().retrieve().body(DiscogsRelease.class));
		} catch (RestClientException e) {
			LOGGER.warn("Could not look up release '{}'.", id, e);
			return Optional.empty();
		}
	}


	public Optional<DiscogsMaster> lookUpMaster(final String id) {
		bucket.asBlocking().consumeUninterruptibly(1);

		try {
			return Optional.ofNullable(restClient.get().uri("/masters/{id}", Map.of("id", id)).retrieve().body(DiscogsMaster.class));
		} catch (RestClientException e) {
			LOGGER.warn("Could not look up master '{}'.", id, e);
			return Optional.empty();
		}
	}

}
