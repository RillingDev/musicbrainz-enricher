package dev.rilling.musicbrainzenricher.api;

import net.jcip.annotations.ThreadSafe;
import org.jetbrains.annotations.NotNull;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Optional;

@Service
@ThreadSafe
public class ScrapingService {

	private static final Logger LOGGER = LoggerFactory.getLogger(ScrapingService.class);

	@NotNull
	public Optional<Document> load(@NotNull String url) {
		try {
			Document document = Jsoup.connect(url).get();
			return Optional.of(document);
		} catch (IOException e) {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.warn("Could not connect to '{}'.", url, e);
			} else {
				LOGGER.warn("Could not connect to '{}'.", url);
			}
			return Optional.empty();
		}
	}
}
