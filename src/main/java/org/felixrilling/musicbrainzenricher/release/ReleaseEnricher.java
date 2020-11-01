package org.felixrilling.musicbrainzenricher.release;

import org.musicbrainz.model.RelationWs2;
import org.springframework.stereotype.Service;

@Service
public interface ReleaseEnricher {
    boolean relationFits(RelationWs2 relationWs2);
}
