package com.innerfriends.userprofilepicture.domain.usecase;

import com.innerfriends.userprofilepicture.domain.*;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.helpers.test.UniAssertSubscriber;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class GetFeaturedUserContentProfilePictureUseCaseTest {

    private ProfilePictureRepository profilePictureRepository;
    private GetFeaturedUserProfilePictureUseCase<Response> getFeaturedUserProfilePictureUseCase;
    private TestResponseTransformer testResponseTransformer;
    private UserProfilePictureCacheRepository userProfilePictureCacheRepository;

    @BeforeEach
    public void setup() {
        profilePictureRepository = mock(ProfilePictureRepository.class);
        userProfilePictureCacheRepository = mock(UserProfilePictureCacheRepository.class);
        getFeaturedUserProfilePictureUseCase = new GetFeaturedUserProfilePictureUseCase<>(profilePictureRepository, userProfilePictureCacheRepository);
        testResponseTransformer = mock(TestResponseTransformer.class);
    }

    @Test
    public void should_get_featured_user_profile_picture_from_cache_when_present() {
        // Given
        final UserPseudo userPseudo = mock(UserPseudo.class);
        final SupportedMediaType supportedMediaType = mock(SupportedMediaType.class);
        final ProfilePictureIdentifier profilePictureIdentifier = mock(ProfilePictureIdentifier.class);
        final CachedUserProfilePictures cachedUserProfilePictures = mock(CachedUserProfilePictures.class);
        doReturn(profilePictureIdentifier).when(cachedUserProfilePictures).featured();
        doReturn(Uni.createFrom().item(cachedUserProfilePictures)).when(userProfilePictureCacheRepository).get(userPseudo);
        final Response response = mock(Response.class);
        doReturn(response).when(testResponseTransformer).toResponse(profilePictureIdentifier);

        // When
        final UniAssertSubscriber<Response> subscriber = getFeaturedUserProfilePictureUseCase.execute(
                new GetFeaturedUserProfilePictureCommand(userPseudo, supportedMediaType),
                testResponseTransformer)
                .subscribe().withSubscriber(UniAssertSubscriber.create());

        // Then
        subscriber.assertCompleted().assertItem(response);
        verify(userProfilePictureCacheRepository, times(1)).get(any());
        verify(cachedUserProfilePictures, times(1)).featured();
        verify(testResponseTransformer).toResponse(any(ProfilePictureIdentifier.class));
        verifyNoMoreInteractions(testResponseTransformer, userProfilePictureCacheRepository, userProfilePictureCacheRepository);
    }

    @Test
    public void should_get_featured_user_profile_picture_from_last_profile_picture_repository_and_cache_it_when_not_in_cache() {
        // Given
        final UserPseudo userPseudo = mock(UserPseudo.class);
        final SupportedMediaType supportedMediaType = mock(SupportedMediaType.class);
        final ProfilePictureIdentifier profilePictureIdentifier = mock(ProfilePictureIdentifier.class);
        final CachedUserProfilePictures cachedUserProfilePictures = mock(CachedUserProfilePictures.class);
        doReturn(Uni.createFrom().nullItem()).when(userProfilePictureCacheRepository).get(userPseudo);
        doReturn(Uni.createFrom().item(profilePictureIdentifier)).when(profilePictureRepository).getLast(userPseudo, supportedMediaType);
        final Response response = mock(Response.class);
        doReturn(response).when(testResponseTransformer).toResponse(profilePictureIdentifier);
        doReturn(Uni.createFrom().nullItem()).when(userProfilePictureCacheRepository).get(userPseudo);
        doReturn(Uni.createFrom().item(cachedUserProfilePictures)).when(userProfilePictureCacheRepository).storeFeatured(userPseudo, profilePictureIdentifier);
        final InOrder inOrder = inOrder(testResponseTransformer, profilePictureRepository, userProfilePictureCacheRepository);

        // When
        final UniAssertSubscriber<Response> subscriber = getFeaturedUserProfilePictureUseCase.execute(
                new GetFeaturedUserProfilePictureCommand(userPseudo, supportedMediaType),
                testResponseTransformer)
                .subscribe().withSubscriber(UniAssertSubscriber.create());

        // Then
        subscriber.assertCompleted().assertItem(response);
        inOrder.verify(userProfilePictureCacheRepository, times(1)).get(any());
        inOrder.verify(profilePictureRepository, times(1)).getLast(any(), any());
        inOrder.verify(userProfilePictureCacheRepository, times(1)).storeFeatured(any(), any(ProfilePictureIdentifier.class));
        inOrder.verify(testResponseTransformer).toResponse(any(ProfilePictureIdentifier.class));
        verifyNoMoreInteractions(testResponseTransformer, userProfilePictureCacheRepository, userProfilePictureCacheRepository);
    }

    @Test
    public void should_handle_profile_picture_not_available_yet_exception_when_getting_last_user_profile_picture() {
        // Given
        final ProfilePictureNotAvailableYetException profilePictureNotAvailableYetException = mock(ProfilePictureNotAvailableYetException.class);
        doReturn(Uni.createFrom().nullItem()).when(userProfilePictureCacheRepository).get(any());
        doReturn(Uni.createFrom().failure(profilePictureNotAvailableYetException)).when(profilePictureRepository).getLast(any(), any());
        final Response response = mock(Response.class);
        doReturn(response).when(testResponseTransformer).toResponse(profilePictureNotAvailableYetException);

        // When
        final UniAssertSubscriber<Response> subscriber = getFeaturedUserProfilePictureUseCase.execute(
                new GetFeaturedUserProfilePictureCommand(mock(UserPseudo.class), mock(SupportedMediaType.class)),
                testResponseTransformer)
                .subscribe().withSubscriber(UniAssertSubscriber.create());

        // Then
        subscriber.assertCompleted().assertItem(response);
        verify(userProfilePictureCacheRepository, times(1)).get(any());
        verify(profilePictureRepository, times(1)).getLast(any(), any());
        verify(testResponseTransformer).toResponse(any(ProfilePictureNotAvailableYetException.class));
        verifyNoMoreInteractions(testResponseTransformer, userProfilePictureCacheRepository, userProfilePictureCacheRepository);
    }

    @Test
    public void should_handle_profile_picture_repository_exception_when_getting_last_user_profile_picture() {
        // Given
        final ProfilePictureRepositoryException profilePictureRepositoryException = mock(ProfilePictureRepositoryException.class);
        doReturn(Uni.createFrom().nullItem()).when(userProfilePictureCacheRepository).get(any());
        doReturn(Uni.createFrom().failure(profilePictureRepositoryException)).when(profilePictureRepository).getLast(any(), any());
        final Response response = mock(Response.class);
        doReturn(response).when(testResponseTransformer).toResponse(profilePictureRepositoryException);

        // When
        final UniAssertSubscriber<Response> subscriber = getFeaturedUserProfilePictureUseCase.execute(
                new GetFeaturedUserProfilePictureCommand(mock(UserPseudo.class), mock(SupportedMediaType.class)),
                testResponseTransformer)
                .subscribe().withSubscriber(UniAssertSubscriber.create());

        // Then
        subscriber.assertCompleted().assertItem(response);
        verify(userProfilePictureCacheRepository, times(1)).get(any());
        verify(profilePictureRepository, times(1)).getLast(any(), any());
        verify(testResponseTransformer).toResponse(any(ProfilePictureRepositoryException.class));
        verifyNoMoreInteractions(testResponseTransformer, userProfilePictureCacheRepository, userProfilePictureCacheRepository);
    }

    @Test
    public void should_handle_runtime_exception_when_getting_last_user_profile_picture() {
        // Given
        final RuntimeException runtimeException = new RuntimeException();
        doReturn(Uni.createFrom().nullItem()).when(userProfilePictureCacheRepository).get(any());
        doReturn(Uni.createFrom().failure(runtimeException)).when(profilePictureRepository).getLast(any(), any());
        final Response response = mock(Response.class);
        doReturn(response).when(testResponseTransformer).toResponse(runtimeException);

        // When
        final UniAssertSubscriber<Response> subscriber = getFeaturedUserProfilePictureUseCase.execute(
                new GetFeaturedUserProfilePictureCommand(mock(UserPseudo.class), mock(SupportedMediaType.class)),
                testResponseTransformer)
                .subscribe().withSubscriber(UniAssertSubscriber.create());

        // Then
        subscriber.assertCompleted().assertItem(response);
        verify(userProfilePictureCacheRepository, times(1)).get(any());
        verify(profilePictureRepository, times(1)).getLast(any(), any());
        verify(testResponseTransformer).toResponse(any(RuntimeException.class));
        verifyNoMoreInteractions(testResponseTransformer, userProfilePictureCacheRepository, userProfilePictureCacheRepository);
    }

    @Test
    public void should_get_featured_user_profile_picture_from_last_profile_picture_repository_when_cache_is_down() {
        // Given
        final UserPseudo userPseudo = mock(UserPseudo.class);
        final SupportedMediaType supportedMediaType = mock(SupportedMediaType.class);
        final ProfilePictureIdentifier profilePictureIdentifier = mock(ProfilePictureIdentifier.class);
        doReturn(Uni.createFrom().failure(RuntimeException::new)).when(userProfilePictureCacheRepository).get(any());
        doReturn(Uni.createFrom().item(profilePictureIdentifier)).when(profilePictureRepository).getLast(any(), any());
        final Response response = mock(Response.class);
        doReturn(response).when(testResponseTransformer).toResponse(profilePictureIdentifier);
        doReturn(Uni.createFrom().failure(new UserProfilePictureNotInCacheException(userPseudo))).when(userProfilePictureCacheRepository).get(any());
        doReturn(Uni.createFrom().failure(RuntimeException::new)).when(userProfilePictureCacheRepository).storeFeatured(any(), any(ProfilePictureIdentifier.class));

        // When
        final UniAssertSubscriber<Response> subscriber = getFeaturedUserProfilePictureUseCase.execute(
                new GetFeaturedUserProfilePictureCommand(userPseudo, supportedMediaType),
                testResponseTransformer)
                .subscribe().withSubscriber(UniAssertSubscriber.create());

        // Then
        subscriber.assertCompleted().assertItem(response);
        verify(userProfilePictureCacheRepository, times(1)).get(any());
        verify(profilePictureRepository, times(1)).getLast(any(), any());
        verify(userProfilePictureCacheRepository, times(1)).storeFeatured(any(), any(ProfilePictureIdentifier.class));
        verify(testResponseTransformer).toResponse(any(ProfilePictureIdentifier.class));
        verifyNoMoreInteractions(testResponseTransformer, userProfilePictureCacheRepository, userProfilePictureCacheRepository);
    }

}
