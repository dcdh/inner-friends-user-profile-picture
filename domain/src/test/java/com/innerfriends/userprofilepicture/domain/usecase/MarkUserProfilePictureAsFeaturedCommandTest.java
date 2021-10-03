package com.innerfriends.userprofilepicture.domain.usecase;

import com.innerfriends.userprofilepicture.domain.SupportedMediaType;
import com.innerfriends.userprofilepicture.domain.UserPseudo;
import com.innerfriends.userprofilepicture.domain.VersionId;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.mock;

public class MarkUserProfilePictureAsFeaturedCommandTest {

    @Test
    public void should_verify_equality() {
        EqualsVerifier.forClass(MarkUserProfilePictureAsFeaturedCommand.class).verify();
    }

    @Test
    public void should_fail_fast_when_user_pseudo_is_null() {
        assertThatThrownBy(() -> new MarkUserProfilePictureAsFeaturedCommand(null, mock(SupportedMediaType.class), mock(VersionId.class)))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    public void should_fail_fast_when_supported_media_type_is_null() {
        assertThatThrownBy(() -> new MarkUserProfilePictureAsFeaturedCommand(mock(UserPseudo.class), null, mock(VersionId.class)))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    public void should_fail_fast_when_version_id_is_null() {
        assertThatThrownBy(() -> new MarkUserProfilePictureAsFeaturedCommand(mock(UserPseudo.class), mock(SupportedMediaType.class), null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    public void should_return_user_pseudo() {
        // Given
        final UserPseudo givenUserPseudo = mock(UserPseudo.class);

        // When && Then
        assertThat(new MarkUserProfilePictureAsFeaturedCommand(givenUserPseudo, mock(SupportedMediaType.class), mock(VersionId.class)).userPseudo())
                .isEqualTo(givenUserPseudo);
    }

    @Test
    public void should_return_supported_media_type() {
        // Given
        final SupportedMediaType givenSupportedMediaType = mock(SupportedMediaType.class);

        // When && Then
        assertThat(new MarkUserProfilePictureAsFeaturedCommand(mock(UserPseudo.class), givenSupportedMediaType, mock(VersionId.class)).mediaType())
                .isEqualTo(givenSupportedMediaType);
    }

    @Test
    public void should_return_version_id() {
        // Given
        final VersionId givenVersionId = mock(VersionId.class);

        // When && Then
        assertThat(new MarkUserProfilePictureAsFeaturedCommand(mock(UserPseudo.class), mock(SupportedMediaType.class), givenVersionId).versionId())
                .isEqualTo(givenVersionId);
    }

}
