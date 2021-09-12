package com.innerfriends.userprofilepicture.domain;

import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class CachedUserProfilePicturesTest {

    @Test
    public void should_not_have_featured_in_cache_when_featured_is_null() {
        // Given
        final CachedUserProfilePictures cachedUserProfilePictures = mock(CachedUserProfilePictures.class);
        doReturn(null).when(cachedUserProfilePictures).featured();
        when(cachedUserProfilePictures.hasFeaturedInCache()).thenCallRealMethod();

        // When && Then
        assertThat(cachedUserProfilePictures.hasFeaturedInCache()).isFalse();
        verify(cachedUserProfilePictures, times(1)).featured();
    }

    @Test
    public void should_have_featured_in_cache_when_featured_is_not_null() {
        // Given
        final CachedUserProfilePictures cachedUserProfilePictures = mock(CachedUserProfilePictures.class);
        doReturn(mock(UserProfilePictureIdentifier.class)).when(cachedUserProfilePictures).featured();
        when(cachedUserProfilePictures.hasFeaturedInCache()).thenCallRealMethod();

        // When && Then
        assertThat(cachedUserProfilePictures.hasFeaturedInCache()).isTrue();
        verify(cachedUserProfilePictures, times(1)).featured();
    }

    @Test
    public void should_not_have_user_profile_picture_identifiers_in_cache_when_list_of_user_profile_pictures_identifiers_is_null() {
        // Given
        final CachedUserProfilePictures cachedUserProfilePictures = mock(CachedUserProfilePictures.class);
        doReturn(null).when(cachedUserProfilePictures).userProfilePictureIdentifiers();
        when(cachedUserProfilePictures.hasUserProfilePictureIdentifiersInCache()).thenCallRealMethod();

        // When && Then
        assertThat(cachedUserProfilePictures.hasUserProfilePictureIdentifiersInCache()).isFalse();
        verify(cachedUserProfilePictures, times(1)).userProfilePictureIdentifiers();
    }

    @Test
    public void should_have_user_profile_picture_identifiers_in_cache_when_list_of_user_profile_pictures_identifiers_is_not_null() {
        // Given
        final CachedUserProfilePictures cachedUserProfilePictures = mock(CachedUserProfilePictures.class);
        doReturn(Collections.emptyList()).when(cachedUserProfilePictures).userProfilePictureIdentifiers();
        when(cachedUserProfilePictures.hasUserProfilePictureIdentifiersInCache()).thenCallRealMethod();

        // When && Then
        assertThat(cachedUserProfilePictures.hasUserProfilePictureIdentifiersInCache()).isTrue();
        verify(cachedUserProfilePictures, times(1)).userProfilePictureIdentifiers();
    }

}
