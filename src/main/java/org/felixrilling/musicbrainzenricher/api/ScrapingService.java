package org.felixrilling.musicbrainzenricher.api;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Optional;

@Service
public class ScrapingService {

    private static final Logger logger = LoggerFactory.getLogger(ScrapingService.class);

    public Optional<Document> load(String url) {
        try {
            Document document = Jsoup.connect(url).get();
            return Optional.of(document);
        } catch (IOException e) {
            if (logger.isDebugEnabled()) {
                logger.warn("Could not connect to '{}': {}.", url, e);
            } else {
                logger.warn("Could not connect to '{}'.", url);
            }
            return Optional.empty();
        }
    }
}
