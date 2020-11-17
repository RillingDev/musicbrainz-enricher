package org.felixrilling.musicbrainzenricher.release;

import org.jetbrains.annotations.NotNull;
import org.musicbrainz.model.RelationWs2;
import org.springframework.stereotype.Service;

@Service
interface ReleaseEnricher {
    boolean relationFits(@NotNull RelationWs2 relationWs2);
}
