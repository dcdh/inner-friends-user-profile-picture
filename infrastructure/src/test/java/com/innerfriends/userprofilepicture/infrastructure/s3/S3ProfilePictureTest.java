package com.innerfriends.userprofilepicture.infrastructure.s3;

import com.innerfriends.userprofilepicture.domain.SupportedMediaType;
import com.innerfriends.userprofilepicture.domain.UserPseudo;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.*;

public class S3ProfilePictureTest {

    @Test
    public void should_verify_equality() {
        EqualsVerifier.forClass(S3ProfilePicture.class).verify();
    }

    @Test
    public void should_fail_fast_when_user_pseudo_is_null() {
        assertThatThrownBy(() -> new S3ProfilePicture(null, mock(ResponseBytes.class)))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    public void should_fail_fast_when_get_object_response_is_null() {
        assertThatThrownBy(() -> new S3ProfilePicture(mock(UserPseudo.class), null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    public void should_return_user_pseudo() {
        // Given
        final UserPseudo givenUserPseudo = mock(UserPseudo.class);
        final ResponseBytes<GetObjectResponse> givenResponseBytes = mock(ResponseBytes.class);
        final GetObjectResponse givenGetObjectResponse = mock(GetObjectResponse.class);
        doReturn(givenGetObjectResponse).when(givenResponseBytes).response();
        doReturn("image/jpeg").when(givenGetObjectResponse).contentType();

        // When && Then
        assertThat(new S3ProfilePicture(givenUserPseudo, givenResponseBytes).userPseudo())
                .isEqualTo(givenUserPseudo);
        verify(givenResponseBytes.response(), times(1)).contentType();
        verify(givenGetObjectResponse, times(1)).contentType();
    }

    @Test
    public void should_return_picture() {
        // Given
        final UserPseudo givenUserPseudo = mock(UserPseudo.class);
        final ResponseBytes<GetObjectResponse> givenResponseBytes = mock(ResponseBytes.class);
        final GetObjectResponse givenGetObjectResponse = mock(GetObjectResponse.class);
        doReturn(givenGetObjectResponse).when(givenResponseBytes).response();
        doReturn("image/jpeg").when(givenGetObjectResponse).contentType();
        doReturn("picture".getBytes()).when(givenResponseBytes).asByteArray();

        // When && Then
        assertThat(new S3ProfilePicture(givenUserPseudo, givenResponseBytes).picture())
                .isEqualTo("picture".getBytes());
        verify(givenResponseBytes.response(), times(1)).contentType();
        verify(givenGetObjectResponse, times(1)).contentType();
        verify(givenResponseBytes, times(1)).asByteArray();
    }

    @Test
    public void should_return_mediaType() {
        // Given
        final UserPseudo givenUserPseudo = mock(UserPseudo.class);
        final ResponseBytes<GetObjectResponse> givenResponseBytes = mock(ResponseBytes.class);
        final GetObjectResponse givenGetObjectResponse = mock(GetObjectResponse.class);
        doReturn(givenGetObjectResponse).when(givenResponseBytes).response();
        doReturn("image/jpeg").when(givenGetObjectResponse).contentType();

        // When && Then
        assertThat(new S3ProfilePicture(givenUserPseudo, givenResponseBytes).mediaType())
                .isEqualTo(SupportedMediaType.IMAGE_JPEG);
        verify(givenResponseBytes.response(), times(1)).contentType();
        verify(givenGetObjectResponse, times(1)).contentType();
    }

    @Test
    public void should_return_content_length() {
        // Given
        final UserPseudo givenUserPseudo = mock(UserPseudo.class);
        final ResponseBytes<GetObjectResponse> givenResponseBytes = mock(ResponseBytes.class);
        final GetObjectResponse givenGetObjectResponse = mock(GetObjectResponse.class);
        doReturn(givenGetObjectResponse).when(givenResponseBytes).response();
        doReturn("image/jpeg").when(givenGetObjectResponse).contentType();
        doReturn(1L).when(givenGetObjectResponse).contentLength();

        // When && Then
        assertThat(new S3ProfilePicture(givenUserPseudo, givenResponseBytes).contentLength())
                .isEqualTo(1L);
        verify(givenResponseBytes.response(), times(1)).contentType();
        verify(givenGetObjectResponse, times(1)).contentType();
        verify(givenGetObjectResponse, times(1)).contentLength();
    }

    @Test
    public void should_return_versionId() {
        // Given
        final UserPseudo givenUserPseudo = mock(UserPseudo.class);
        final ResponseBytes<GetObjectResponse> givenResponseBytes = mock(ResponseBytes.class);
        final GetObjectResponse givenGetObjectResponse = mock(GetObjectResponse.class);
        doReturn(givenGetObjectResponse).when(givenResponseBytes).response();
        doReturn("image/jpeg").when(givenGetObjectResponse).contentType();
        doReturn("v0").when(givenGetObjectResponse).versionId();

        // When && Then
        assertThat(new S3ProfilePicture(givenUserPseudo, givenResponseBytes).versionId())
                .isEqualTo("v0");
        verify(givenResponseBytes.response(), times(1)).contentType();
        verify(givenGetObjectResponse, times(1)).contentType();
        verify(givenGetObjectResponse, times(1)).versionId();
    }

}
