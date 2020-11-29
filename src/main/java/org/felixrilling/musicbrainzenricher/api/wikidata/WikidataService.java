package org.felixrilling.musicbrainzenricher.api.wikidata;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.wikidata.wdtk.datamodel.interfaces.EntityDocument;
import org.wikidata.wdtk.datamodel.interfaces.ItemDocument;
import org.wikidata.wdtk.datamodel.interfaces.Statement;
import org.wikidata.wdtk.datamodel.interfaces.StatementGroup;
import org.wikidata.wdtk.wikibaseapi.WikibaseDataFetcher;
import org.wikidata.wdtk.wikibaseapi.apierrors.MediaWikiApiErrorException;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
public class WikidataService {
    private static final Logger logger = LoggerFactory.getLogger(WikidataService.class);

    private final WikibaseDataFetcher fetcher = WikibaseDataFetcher.getWikidataDataFetcher();

    public Optional<List<Statement>> findEntityPropertyValues(String entityId, String propertyId) {
        EntityDocument entityDocument;
        try {
            entityDocument = fetcher.getEntityDocument(entityId);
        } catch (MediaWikiApiErrorException | IOException e) {
            logger.error("Could not fetch document: '{}'.", entityId, e);
            return Optional.empty();
        }
        if (!(entityDocument instanceof ItemDocument)) {
            logger.warn("Unexpected document: '{}'.", entityDocument);
            return Optional.empty();
        }

        StatementGroup statementGroup = ((ItemDocument) entityDocument).findStatementGroup(propertyId);
        if (statementGroup == null) {
            return Optional.empty();
        }
        return Optional.of(statementGroup.getStatements());
    }
}