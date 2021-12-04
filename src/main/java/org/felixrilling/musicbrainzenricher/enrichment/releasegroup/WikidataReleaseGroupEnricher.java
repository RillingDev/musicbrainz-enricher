package org.felixrilling.musicbrainzenricher.enrichment.releasegroup;

import org.felixrilling.musicbrainzenricher.api.wikidata.WikidataService;
import org.felixrilling.musicbrainzenricher.core.DataType;
import org.felixrilling.musicbrainzenricher.core.GenreRepository;
import org.felixrilling.musicbrainzenricher.enrichment.GenreEnricher;
import org.felixrilling.musicbrainzenricher.util.RegexUtils;
import org.jetbrains.annotations.NotNull;
import org.musicbrainz.model.RelationWs2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.wikidata.wdtk.datamodel.interfaces.EntityIdValue;
import org.wikidata.wdtk.datamodel.interfaces.Statement;
import org.wikidata.wdtk.datamodel.interfaces.StringValue;
import org.wikidata.wdtk.datamodel.interfaces.Value;

import java.util.*;
import java.util.regex.Pattern;

/**
 * The simplified logic looks like this:
 * <ol>
 *     <li>Extract wikidata ID from URL</li>
 *     <li>Look up wikidata release entity by ID</li>
 *     <li>Find genre statements</li>
 *     <li>For every genre statement, get its id</li>
 *     <li>For every genre id, find the genre entity by ID</li>
 *     <li>For every genre entity, find the musicbrainz link statement</li>
 *     <li>For every genre entities musicbrainz link statement, look up its name by the MBID against the musicbrainz database</li>
 *     <li>Return the names found in the musicbrainz database</li>
 * </ol>
 * <p>
 * Also see https://www.mediawiki.org/wiki/Wikidata_Toolkit
 */
// https://musicbrainz.org/release-group/a63e5fa6-d6ad-47bd-986d-4a27b0c9de70
// https://www.wikidata.org/wiki/Q53020187
@Service
class WikidataReleaseGroupEnricher implements GenreEnricher {

	private static final Logger LOGGER = LoggerFactory.getLogger(WikidataReleaseGroupEnricher.class);

	private static final String GENRE_PROPERTY_ID = "P136";
	private static final String MUSICBRAINZ_LINK_PROPERTY_ID = "P8052";

	private static final Pattern ID_REGEX = Pattern.compile(".+/(?<id>Q\\d+)$");

	private final WikidataService wikidataService;
	private final GenreRepository genreRepository;

	WikidataReleaseGroupEnricher(WikidataService wikidataService, GenreRepository genreRepository) {
		this.wikidataService = wikidataService;
		this.genreRepository = genreRepository;
	}

	@Override
	@NotNull
	public Set<String> fetchGenres(@NotNull RelationWs2 relation) {
		Optional<String> id = RegexUtils.maybeGroup(ID_REGEX.matcher(relation.getTargetId()), "id");
		if (id.isEmpty()) {
			LOGGER.warn("Could not find ID in '{}'.", relation.getTargetId());
			return Set.of();
		}
		// We can skip genre matching as we use the genre names directly from Musicbrainz.
		return wikidataService.findEntityPropertyValues(id.get(), GENRE_PROPERTY_ID)
			.map(this::extractGenreNames)
			.orElse(Set.of());
	}

	@NotNull
	private Set<String> extractGenreNames(@NotNull List<Statement> genreStatements) {
		Set<String> genres = new HashSet<>(genreStatements.size());
		for (Statement genreStatement : genreStatements) {
			if (!(genreStatement.getValue() instanceof EntityIdValue)) {
				LOGGER.warn("Unexpected genre statement type: '{}'.", genreStatement);
			} else {
				findGenreName(((EntityIdValue) genreStatement.getValue()).getId()).ifPresent(genres::add);
			}
		}
		return genres;
	}

	@NotNull
	private Optional<String> findGenreName(@NotNull String genreId) {
		Optional<List<Statement>> musicbrainzLinkStatements = wikidataService.findEntityPropertyValues(genreId,
			MUSICBRAINZ_LINK_PROPERTY_ID);
		if (musicbrainzLinkStatements.map(List::isEmpty).orElse(true)) {
			LOGGER.warn("No musicbrainz link found for genre: '{}'.", genreId);
			return Optional.empty();
		}
		Value value = musicbrainzLinkStatements.get().get(0).getValue();
		if (!(value instanceof StringValue)) {
			LOGGER.warn("Unexpected musicbrainz link type: '{}'.", value);
			return Optional.empty();
		}
		UUID mbid = UUID.fromString(((StringValue) value).getString());
		return Optional.ofNullable(genreRepository.findGenreNameByMbid(mbid));
	}

	@Override
	public boolean isRelationSupported(@NotNull RelationWs2 relation) {
		return "http://musicbrainz.org/ns/rel-2.0#wikidata".equals(relation.getType()) &&
			"http://musicbrainz.org/ns/rel-2.0#url".equals(relation.getTargetType());
	}

	@Override
	@NotNull
	public DataType getDataType() {
		return DataType.RELEASE_GROUP;
	}
}
