package org.felixrilling.musicbrainzenricher.api.wikidata;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.wikidata.wdtk.datamodel.helpers.Datamodel;
import org.wikidata.wdtk.datamodel.interfaces.*;
import org.wikidata.wdtk.wikibaseapi.BasicApiConnection;
import org.wikidata.wdtk.wikibaseapi.WikibaseDataFetcher;
import org.wikidata.wdtk.wikibaseapi.apierrors.MediaWikiApiErrorException;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class WikidataService {
    private static final Logger logger = LoggerFactory.getLogger(WikidataService.class);

    private final BasicApiConnection wikidataApiConnection = BasicApiConnection.getWikidataApiConnection();

    public @NotNull Optional<List<Statement>> findEntityPropertyValues(@NotNull String entityId, @NotNull String propertyId) {
        PropertyIdValue propertyIdValue = Datamodel.makeWikidataPropertyIdValue(propertyId);

        WikibaseDataFetcher fetcher = new WikibaseDataFetcher(wikidataApiConnection, Datamodel.SITE_WIKIDATA);
        fetcher.getFilter().excludeAllLanguages();
        fetcher.getFilter().excludeAllSiteLinks();
        fetcher.getFilter().setPropertyFilter(Set.of(propertyIdValue));

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

        StatementGroup statementGroup = ((ItemDocument) entityDocument).findStatementGroup(propertyIdValue);
        if (statementGroup == null) {
            return Optional.empty();
        }
        return Optional.of(statementGroup.getStatements());
    }
}