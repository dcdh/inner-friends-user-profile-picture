package com.innerfriends.userprofilepicture.domain.usecase;

import com.innerfriends.userprofilepicture.domain.SupportedMediaType;
import com.innerfriends.userprofilepicture.domain.UserPseudo;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.mock;

public class GetLastUserProfilePictureCommandTest {

    @Test
    public void should_verify_equality() {
        EqualsVerifier.forClass(GetLastUserProfilePictureCommand.class).verify();
    }

    @Test
    public void should_fail_fast_when_user_pseudo_is_null() {
        assertThatThrownBy(() -> new GetLastUserProfilePictureCommand(null, mock(SupportedMediaType.class)))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    public void should_fail_fast_when_media_type_is_null() {
        assertThatThrownBy(() -> new GetLastUserProfilePictureCommand(mock(UserPseudo.class), null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    public void should_return_user_pseudo() {
        // Given
        final UserPseudo givenUserPseudo = mock(UserPseudo.class);

        // When && Then
        assertThat(new GetLastUserProfilePictureCommand(givenUserPseudo, mock(SupportedMediaType.class)).userPseudo())
                .isEqualTo(givenUserPseudo);
    }

    @Test
    public void should_return_media_type() {
        // Given
        final SupportedMediaType givenSupportedMediaType = mock(SupportedMediaType.class);

        // When && Then
        assertThat(new GetLastUserProfilePictureCommand(mock(UserPseudo.class), givenSupportedMediaType).mediaType())
                .isEqualTo(givenSupportedMediaType);
    }

}
