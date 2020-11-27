package org.felixrilling.musicbrainzenricher.enrichment.genre;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;
import java.util.TreeSet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GenreMatcherServiceTest {

    @Mock
    private GenreProviderService genreProviderService;

    @InjectMocks
    private GenreMatcherService genreMatcherService;

    @Test
    void test() {
        Set<String> genres = new TreeSet<>(Set.of("idm", "edm", "electronic", "hip-hop"));
        when(genreProviderService.getGenres()).thenReturn(genres);

        assertThat(genreMatcherService.match(Set.of("edm", "electron"))).containsExactly("edm");
        assertThat(genreMatcherService.match(Set.of("ebm", "electroni"))).containsExactly("electronic");
        assertThat(genreMatcherService.match(Set.of("hip hop"))).containsExactly("hip-hop");
    }
}