package com.innerfriends.userprofilepicture.domain.usecase;

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
    public void should_fail_fast_when_metadata_is_null() {
        assertThatThrownBy(() -> new GetLastUserProfilePictureCommand(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    public void should_return_content() {
        // Given
        final UserPseudo givenUserPseudo = mock(UserPseudo.class);

        // When && Then
        assertThat(new GetLastUserProfilePictureCommand(givenUserPseudo).userPseudo())
                .isEqualTo(givenUserPseudo);
    }

}
