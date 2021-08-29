package com.innerfriends.userprofilepicture.domain.usecase;

import com.innerfriends.userprofilepicture.domain.SupportedMediaType;
import com.innerfriends.userprofilepicture.domain.UserPseudo;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.mock;

public class SaveUserProfilePictureCommandTest {

    @Test
    public void should_verify_equality() {
        EqualsVerifier.forClass(SaveUserProfilePictureCommand.class).verify();
    }

    @Test
    public void should_fail_fast_when_user_pseudo_is_null() {
        assertThatThrownBy(() -> new SaveUserProfilePictureCommand(null, "picture".getBytes(), SupportedMediaType.IMAGE_JPEG))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    public void should_fail_fast_when_picture_is_null() {
        assertThatThrownBy(() -> new SaveUserProfilePictureCommand(mock(UserPseudo.class), null, SupportedMediaType.IMAGE_JPEG))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    public void should_fail_fast_when_supported_media_type_is_null() {
        assertThatThrownBy(() -> new SaveUserProfilePictureCommand(mock(UserPseudo.class), "picture".getBytes(), null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    public void should_return_user_pseudo() {
        // Given
        final UserPseudo givenUserPseudo = mock(UserPseudo.class);

        // When && Then
        assertThat(new SaveUserProfilePictureCommand(givenUserPseudo, "picture".getBytes(), SupportedMediaType.IMAGE_JPEG).userPseudo())
                .isEqualTo(givenUserPseudo);
    }

    @Test
    public void should_return_picture() {
        // Given

        // When && Then
        assertThat(new SaveUserProfilePictureCommand(mock(UserPseudo.class), "picture".getBytes(), SupportedMediaType.IMAGE_JPEG).picture())
                .isEqualTo("picture".getBytes());
    }

    @Test
    public void should_return_media_type() {
        // Given

        // When && Then
        assertThat(new SaveUserProfilePictureCommand(mock(UserPseudo.class), "picture".getBytes(), SupportedMediaType.IMAGE_JPEG).mediaType())
                .isEqualTo(SupportedMediaType.IMAGE_JPEG);
    }

}
