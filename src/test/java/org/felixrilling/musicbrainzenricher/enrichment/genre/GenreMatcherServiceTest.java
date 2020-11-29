package org.felixrilling.musicbrainzenricher.enrichment.genre;

import org.felixrilling.musicbrainzenricher.api.musicbrainz.MusicbrainzDbQueryService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GenreMatcherServiceTest {

    @Mock
    private MusicbrainzDbQueryService musicbrainzDbQueryService;

    @InjectMocks
    private GenreMatcherService genreMatcherService;

    @Test
    void test() {
        List<String> genres = List.of("idm", "edm", "electronic", "hip-hop");
        when(musicbrainzDbQueryService.queryGenreNames()).thenReturn(genres);

        assertThat(genreMatcherService.match(Set.of("edm", "electron"))).containsExactly("edm");
        assertThat(genreMatcherService.match(Set.of("ebm", "electroni"))).containsExactly("electronic");
        assertThat(genreMatcherService.match(Set.of("hip hop"))).containsExactly("hip-hop");
    }
}