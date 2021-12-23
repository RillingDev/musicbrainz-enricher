package dev.rilling.musicbrainzenricher.core.genre;

import dev.rilling.musicbrainzenricher.core.GenreRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GenreMatcherServiceTest {

	@Mock
	private GenreRepository genreRepository;

	@InjectMocks
	private GenreMatcherService genreMatcherService;

	@Test
	@DisplayName("Corrected name is returned.")
	void matchReturnsCorrected() {
		Set<String> genres = Set.of("hip-hop", "drum and bass", "electronic");
		when(genreRepository.findGenreNames()).thenReturn(genres);

		assertThat(genreMatcherService.match(Set.of("hip hop"))).containsExactly("hip-hop");
		assertThat(genreMatcherService.match(Set.of("drum & bass", "electronic"))).containsExactlyInAnyOrder(
			"drum and bass",
			"electronic");
	}

	@Test
	@DisplayName("Not matching items are skipped.")
	void matchSkipsNotMatching() {
		Set<String> genres = Set.of("edm", "hardcore");
		when(genreRepository.findGenreNames()).thenReturn(genres);

		assertThat(genreMatcherService.match(Set.of("ebm"))).isEmpty();
		assertThat(genreMatcherService.match(Set.of("bardcore"))).isEmpty();
	}
}
