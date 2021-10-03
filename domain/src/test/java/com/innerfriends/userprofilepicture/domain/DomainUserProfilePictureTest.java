package com.innerfriends.userprofilepicture.domain;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.*;

public class DomainUserProfilePictureTest {

    @Test
    public void should_verify_equality() {
        EqualsVerifier.forClass(DomainUserProfilePicture.class).verify();
    }

    @Test
    public void should_fail_fast_when_user_pseudo_is_null() {
        // Given
        final UserProfilePictureIdentifier userProfilePictureIdentifier = mock(UserProfilePictureIdentifier.class);
        doReturn(null).when(userProfilePictureIdentifier).userPseudo();

        // When && Then
        assertThatThrownBy(() -> new DomainUserProfilePicture(userProfilePictureIdentifier, Boolean.TRUE))
                .isInstanceOf(NullPointerException.class);
        verify(userProfilePictureIdentifier, times(1)).userPseudo();
        verifyNoMoreInteractions(userProfilePictureIdentifier);
    }

    @Test
    public void should_fail_fast_when_supported_media_type_is_null() {
        // Given
        final UserProfilePictureIdentifier userProfilePictureIdentifier = mock(UserProfilePictureIdentifier.class);
        doReturn(mock(UserPseudo.class)).when(userProfilePictureIdentifier).userPseudo();
        doReturn(null).when(userProfilePictureIdentifier).mediaType();

        // When && Then
        assertThatThrownBy(() -> new DomainUserProfilePicture(userProfilePictureIdentifier, Boolean.TRUE))
                .isInstanceOf(NullPointerException.class);
        verify(userProfilePictureIdentifier, times(1)).userPseudo();
        verify(userProfilePictureIdentifier, times(1)).mediaType();
        verifyNoMoreInteractions(userProfilePictureIdentifier);
    }

    @Test
    public void should_fail_fast_when_version_id_is_null() {
        // Given
        final UserProfilePictureIdentifier userProfilePictureIdentifier = mock(UserProfilePictureIdentifier.class);
        doReturn(mock(UserPseudo.class)).when(userProfilePictureIdentifier).userPseudo();
        doReturn(mock(SupportedMediaType.class)).when(userProfilePictureIdentifier).mediaType();
        doReturn(null).when(userProfilePictureIdentifier).versionId();

        // When && Then
        assertThatThrownBy(() -> new DomainUserProfilePicture(userProfilePictureIdentifier, Boolean.TRUE))
                .isInstanceOf(NullPointerException.class);
        verify(userProfilePictureIdentifier, times(1)).userPseudo();
        verify(userProfilePictureIdentifier, times(1)).mediaType();
        verify(userProfilePictureIdentifier, times(1)).versionId();
    }

    @Test
    public void should_fail_fast_when_featured_is_null() {
        // Given
        final UserProfilePictureIdentifier userProfilePictureIdentifier = mock(UserProfilePictureIdentifier.class);
        doReturn(mock(UserPseudo.class)).when(userProfilePictureIdentifier).userPseudo();
        doReturn(mock(SupportedMediaType.class)).when(userProfilePictureIdentifier).mediaType();
        doReturn(mock(VersionId.class)).when(userProfilePictureIdentifier).versionId();

        // When && Then
        assertThatThrownBy(() -> new DomainUserProfilePicture(userProfilePictureIdentifier, (Boolean) null))
                .isInstanceOf(NullPointerException.class);
        verify(userProfilePictureIdentifier, times(1)).userPseudo();
        verify(userProfilePictureIdentifier, times(1)).mediaType();
        verify(userProfilePictureIdentifier, times(1)).versionId();
    }

    @Test
    public void should_be_featured_when_user_profile_picture_identifier_is_featured() {
        // Given
        final UserProfilePictureIdentifier userProfilePictureIdentifier = mock(UserProfilePictureIdentifier.class);
        doReturn(mock(UserPseudo.class)).when(userProfilePictureIdentifier).userPseudo();
        doReturn(mock(SupportedMediaType.class)).when(userProfilePictureIdentifier).mediaType();
        doReturn(mock(VersionId.class)).when(userProfilePictureIdentifier).versionId();

        // When && Then
        assertThat(new DomainUserProfilePicture(userProfilePictureIdentifier, userProfilePictureIdentifier).isFeatured())
                .isTrue();
        verify(userProfilePictureIdentifier, times(1)).userPseudo();
        verify(userProfilePictureIdentifier, times(1)).mediaType();
        verify(userProfilePictureIdentifier, times(1)).versionId();
    }

    @Test
    public void should_not_be_featured_when_user_profile_picture_identifier_is_not_featured() {
        // Given
        final UserProfilePictureIdentifier userProfilePictureIdentifier = mock(UserProfilePictureIdentifier.class);
        doReturn(mock(UserPseudo.class)).when(userProfilePictureIdentifier).userPseudo();
        doReturn(mock(SupportedMediaType.class)).when(userProfilePictureIdentifier).mediaType();
        doReturn(mock(VersionId.class)).when(userProfilePictureIdentifier).versionId();

        // When && Then
        assertThat(new DomainUserProfilePicture(userProfilePictureIdentifier, mock(UserProfilePictureIdentifier.class)).isFeatured())
                .isFalse();
        verify(userProfilePictureIdentifier, times(1)).userPseudo();
        verify(userProfilePictureIdentifier, times(1)).mediaType();
        verify(userProfilePictureIdentifier, times(1)).versionId();
    }

    @Test
    public void should_return_user_pseudo() {
        // Given
        final UserProfilePictureIdentifier userProfilePictureIdentifier = mock(UserProfilePictureIdentifier.class);
        final UserPseudo givenUserPseudo = mock(UserPseudo.class);
        doReturn(givenUserPseudo).when(userProfilePictureIdentifier).userPseudo();
        doReturn(mock(SupportedMediaType.class)).when(userProfilePictureIdentifier).mediaType();
        doReturn(mock(VersionId.class)).when(userProfilePictureIdentifier).versionId();

        // When && Then
        assertThat(new DomainUserProfilePicture(userProfilePictureIdentifier, Boolean.TRUE).userPseudo())
                .isEqualTo(givenUserPseudo);
        verify(userProfilePictureIdentifier, times(1)).userPseudo();
        verify(userProfilePictureIdentifier, times(1)).mediaType();
        verify(userProfilePictureIdentifier, times(1)).versionId();
    }

    @Test
    public void should_return_media_type() {
        // Given
        final UserProfilePictureIdentifier userProfilePictureIdentifier = mock(UserProfilePictureIdentifier.class);
        doReturn(mock(UserPseudo.class)).when(userProfilePictureIdentifier).userPseudo();
        final SupportedMediaType givenSupportedMediaType = mock(SupportedMediaType.class);
        doReturn(givenSupportedMediaType).when(userProfilePictureIdentifier).mediaType();
        doReturn(mock(VersionId.class)).when(userProfilePictureIdentifier).versionId();

        // When && Then
        assertThat(new DomainUserProfilePicture(userProfilePictureIdentifier, Boolean.TRUE).mediaType())
                .isEqualTo(givenSupportedMediaType);
        verify(userProfilePictureIdentifier, times(1)).userPseudo();
        verify(userProfilePictureIdentifier, times(1)).mediaType();
        verify(userProfilePictureIdentifier, times(1)).versionId();
    }

    @Test
    public void should_return_version_id() {
        // Given
        final UserProfilePictureIdentifier userProfilePictureIdentifier = mock(UserProfilePictureIdentifier.class);
        doReturn(mock(UserPseudo.class)).when(userProfilePictureIdentifier).userPseudo();
        doReturn(mock(SupportedMediaType.class)).when(userProfilePictureIdentifier).mediaType();
        final VersionId givenVersionId = mock(VersionId.class);
        doReturn(givenVersionId).when(userProfilePictureIdentifier).versionId();

        // When && Then
        assertThat(new DomainUserProfilePicture(userProfilePictureIdentifier, Boolean.TRUE).versionId())
                .isEqualTo(givenVersionId);
        verify(userProfilePictureIdentifier, times(1)).userPseudo();
        verify(userProfilePictureIdentifier, times(1)).mediaType();
        verify(userProfilePictureIdentifier, times(1)).versionId();
    }

}
