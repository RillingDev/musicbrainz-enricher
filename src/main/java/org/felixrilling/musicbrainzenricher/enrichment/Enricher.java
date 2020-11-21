package org.felixrilling.musicbrainzenricher.enrichment;

import org.felixrilling.musicbrainzenricher.DataType;
import org.jetbrains.annotations.NotNull;
import org.musicbrainz.model.RelationWs2;
import org.springframework.stereotype.Component;

@Component
public interface Enricher {
    boolean dataTypeFits(@NotNull DataType dataType);

    boolean relationFits(@NotNull RelationWs2 relationWs2);

}
