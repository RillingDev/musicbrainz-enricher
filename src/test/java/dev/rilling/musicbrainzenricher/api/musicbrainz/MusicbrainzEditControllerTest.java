package dev.rilling.musicbrainzenricher.api.musicbrainz;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.musicbrainz.DomainsWs2;
import org.musicbrainz.model.entity.EntityWs2;
import org.musicbrainz.model.entity.ReleaseGroupWs2;
import org.musicbrainz.utils.MbUtils;

import java.util.Set;
import java.util.UUID;

import static dev.rilling.musicbrainzenricher.api.musicbrainz.MusicbrainzEditController.TAG_SUBMISSION_SIZE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MusicbrainzEditControllerTest {

	@InjectMocks
	MusicbrainzEditController musicbrainzEditController;

	@Mock
	MusicbrainzEditService musicbrainzEditService;

	@Captor
	ArgumentCaptor<Set<EntityWs2>> submissionCaptor;

	@Test
	@DisplayName("submission takes place only after TAG_SUBMISSION_SIZE items.")
	void submissionAfterItemCount() throws MusicbrainzException {

		int submissionCountThatDoesNotTriggerSubmit = TAG_SUBMISSION_SIZE - 1;
		for (int i = 0; i < submissionCountThatDoesNotTriggerSubmit; i++) {
			musicbrainzEditController.submitReleaseGroupUserTags(createReleaseGroup(), Set.of("foo"));
		}
		// After TAG_SUBMISSION_SIZE - 1 items, the submission size has not been reached,
		// so no submission should have happened.
		verify(musicbrainzEditService, never()).submitUserTags(anySet());

		musicbrainzEditController.submitReleaseGroupUserTags(createReleaseGroup(), Set.of("foo"));

		// Now we reached the limit, so one submit is expected.
		verify(musicbrainzEditService).submitUserTags(submissionCaptor.capture());
		assertThat(submissionCaptor.getValue()).hasSize(TAG_SUBMISSION_SIZE);
	}

	@Test
	@DisplayName("submission may take place early if flush() is called.")
	void submissionAfterFlush() throws MusicbrainzException {

		int submissionCountThatDoesNotTriggerSubmit = TAG_SUBMISSION_SIZE - 1;
		for (int i = 0; i < submissionCountThatDoesNotTriggerSubmit; i++) {
			musicbrainzEditController.submitReleaseGroupUserTags(createReleaseGroup(), Set.of("foo"));
		}

		verify(musicbrainzEditService, never()).submitUserTags(anySet());

		musicbrainzEditController.flush();

		verify(musicbrainzEditService).submitUserTags(submissionCaptor.capture());
		assertThat(submissionCaptor.getValue()).hasSize(submissionCountThatDoesNotTriggerSubmit);
	}

	@Test
	@DisplayName("submission do not take place if flush() is called if none exist.")
	void submissionAfterFlushEmpty() throws MusicbrainzException {
		musicbrainzEditController.flush();

		verify(musicbrainzEditService, never()).submitUserTags(anySet());
	}

	@NotNull
	private ReleaseGroupWs2 createReleaseGroup() {
		ReleaseGroupWs2 releaseGroupWs2 = new ReleaseGroupWs2();
		UUID mbid = UUID.randomUUID();
		releaseGroupWs2.setIdUri(MbUtils.convertIdToURI(mbid.toString(), DomainsWs2.RELEASEGROUP));
		return releaseGroupWs2;
	}
}
