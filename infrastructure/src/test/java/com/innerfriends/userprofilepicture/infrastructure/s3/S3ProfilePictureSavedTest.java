package com.innerfriends.userprofilepicture.infrastructure.s3;

import com.innerfriends.userprofilepicture.domain.UserPseudo;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.*;

public class S3ProfilePictureSavedTest {

    @Test
    public void should_verify_equality() {
        EqualsVerifier.forClass(S3ProfilePictureSaved.class).verify();
    }

    @Test
    public void should_fail_fast_when_user_pseudo_is_null() {
        assertThatThrownBy(() -> new S3ProfilePictureSaved(null, mock(PutObjectResponse.class)))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    public void should_fail_fast_when_put_object_response_is_null() {
        assertThatThrownBy(() -> new S3ProfilePictureSaved(mock(UserPseudo.class), null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    public void should_return_user_pseudo() {
        // Given
        final UserPseudo givenUserPseudo = mock(UserPseudo.class);

        // When && Then
        assertThat(new S3ProfilePictureSaved(givenUserPseudo, mock(PutObjectResponse.class)).userPseudo())
                .isEqualTo(givenUserPseudo);
    }

    @Test
    public void should_return_versionId() {
        // Given
        final PutObjectResponse givenPutObjectResponse = mock(PutObjectResponse.class);
        doReturn("versionId").when(givenPutObjectResponse).versionId();

        // When && Then
        assertThat(new S3ProfilePictureSaved(mock(UserPseudo.class), givenPutObjectResponse).versionId())
                .isEqualTo("versionId");
        verify(givenPutObjectResponse, times(1)).versionId();
    }

}
