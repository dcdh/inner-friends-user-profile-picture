package com.innerfriends.userprofilepicture.domain.usecase;

import com.innerfriends.userprofilepicture.domain.*;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.helpers.test.UniAssertSubscriber;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class GetFeaturedUserProfilePictureUseCaseTest {

    private UserProfilePictureRepository userProfilePictureRepository;
    private GetFeaturedUserProfilePictureUseCase<Response> getFeaturedUserProfilePictureUseCase;
    private TestResponseTransformer testResponseTransformer;
    private UserProfilePictureCacheRepository userProfilePictureCacheRepository;
    private UserProfilPictureFeaturedRepository userProfilPictureFeaturedRepository;
    private LockMechanism lockMechanism;

    @BeforeEach
    public void setup() {
        userProfilePictureRepository = mock(UserProfilePictureRepository.class);
        userProfilePictureCacheRepository = mock(UserProfilePictureCacheRepository.class);
        lockMechanism = mock(LockMechanism.class);
        userProfilPictureFeaturedRepository = mock(UserProfilPictureFeaturedRepository.class);
        getFeaturedUserProfilePictureUseCase = new GetFeaturedUserProfilePictureUseCase<>(userProfilePictureRepository, userProfilePictureCacheRepository,
                lockMechanism, userProfilPictureFeaturedRepository);
        testResponseTransformer = mock(TestResponseTransformer.class);
    }

    @Test
    public void should_get_featured_user_profile_picture_from_cache_when_present() {
        // Given
        final UserPseudo userPseudo = mock(UserPseudo.class);
        final SupportedMediaType supportedMediaType = mock(SupportedMediaType.class);
        final UserProfilePictureIdentifier userProfilePictureIdentifier = mock(UserProfilePictureIdentifier.class);
        final CachedUserProfilePictures cachedUserProfilePictures = mock(CachedUserProfilePictures.class);
        doReturn(true).when(cachedUserProfilePictures).hasFeaturedInCache();
        doReturn(userProfilePictureIdentifier).when(cachedUserProfilePictures).featured();
        doReturn(Uni.createFrom().item(cachedUserProfilePictures)).when(userProfilePictureCacheRepository).get(userPseudo);
        final Response response = mock(Response.class);
        doReturn(response).when(testResponseTransformer).toResponse(userProfilePictureIdentifier);
        doReturn(Uni.createFrom().nullItem()).when(lockMechanism).lock(userPseudo);

        // When
        final UniAssertSubscriber<Response> subscriber = getFeaturedUserProfilePictureUseCase.execute(
                new GetFeaturedUserProfilePictureCommand(userPseudo, supportedMediaType),
                testResponseTransformer)
                .subscribe().withSubscriber(UniAssertSubscriber.create());

        // Then
        subscriber.assertCompleted().assertItem(response);
        verify(lockMechanism, times(1)).lock(userPseudo);
        verify(userProfilePictureCacheRepository, times(1)).get(any());
        verify(cachedUserProfilePictures, times(1)).hasFeaturedInCache();
        verify(cachedUserProfilePictures, times(1)).featured();
        verify(testResponseTransformer).toResponse(any(UserProfilePictureIdentifier.class));
        verify(lockMechanism, times(1)).unlock(userPseudo);
        verifyNoMoreInteractions(testResponseTransformer, userProfilePictureCacheRepository, userProfilPictureFeaturedRepository, userProfilePictureCacheRepository, lockMechanism);
    }

    @Test
    public void should_get_featured_from_user_profil_picture_featured_repository_and_cache_it_when_not_in_cache() {
        // Given
        final UserPseudo userPseudo = mock(UserPseudo.class);
        final SupportedMediaType supportedMediaType = mock(SupportedMediaType.class);
        final UserProfilePictureIdentifier userProfilePictureIdentifier = mock(UserProfilePictureIdentifier.class);
        final CachedUserProfilePictures cachedUserProfilePictures = mock(CachedUserProfilePictures.class);
        doReturn(userProfilePictureIdentifier).when(cachedUserProfilePictures).featured();
        doReturn(Uni.createFrom().nullItem()).when(userProfilePictureCacheRepository).get(userPseudo);
        doReturn(Uni.createFrom().item(userProfilePictureIdentifier)).when(userProfilPictureFeaturedRepository).getFeatured(userPseudo);
        final Response response = mock(Response.class);
        doReturn(response).when(testResponseTransformer).toResponse(userProfilePictureIdentifier);
        doReturn(Uni.createFrom().nullItem()).when(userProfilePictureCacheRepository).get(userPseudo);
        doReturn(Uni.createFrom().item(cachedUserProfilePictures)).when(userProfilePictureCacheRepository).storeFeatured(userPseudo, userProfilePictureIdentifier);
        doReturn(Uni.createFrom().nullItem()).when(lockMechanism).lock(userPseudo);
        final InOrder inOrder = inOrder(testResponseTransformer, userProfilePictureRepository, userProfilPictureFeaturedRepository, userProfilePictureCacheRepository, lockMechanism);

        // When
        final UniAssertSubscriber<Response> subscriber = getFeaturedUserProfilePictureUseCase.execute(
                new GetFeaturedUserProfilePictureCommand(userPseudo, supportedMediaType),
                testResponseTransformer)
                .subscribe().withSubscriber(UniAssertSubscriber.create());

        // Then
        subscriber.assertCompleted().assertItem(response);
        inOrder.verify(lockMechanism, times(1)).lock(userPseudo);
        inOrder.verify(userProfilePictureCacheRepository, times(1)).get(any());
        inOrder.verify(userProfilPictureFeaturedRepository, times(1)).getFeatured(any());
        inOrder.verify(userProfilePictureCacheRepository, times(1)).storeFeatured(any(), any(UserProfilePictureIdentifier.class));
        inOrder.verify(testResponseTransformer).toResponse(any(UserProfilePictureIdentifier.class));
        inOrder.verify(lockMechanism, times(1)).unlock(userPseudo);
        verify(cachedUserProfilePictures, times(1)).featured();
        verifyNoMoreInteractions(testResponseTransformer, userProfilePictureCacheRepository, userProfilPictureFeaturedRepository, userProfilePictureCacheRepository, lockMechanism);
    }

    @Test
    public void should_get_last_user_profile_picture_from_user_profile_picture_repository_when_not_in_cache_and_not_stored_yet() {
        // Given
        final UserPseudo userPseudo = mock(UserPseudo.class);
        final SupportedMediaType supportedMediaType = mock(SupportedMediaType.class);
        final UserProfilePictureIdentifier userProfilePictureIdentifier = mock(UserProfilePictureIdentifier.class);
        doReturn(Uni.createFrom().nullItem()).when(userProfilePictureCacheRepository).get(userPseudo);
        doReturn(Uni.createFrom().failure(new NoUserProfilPictureFeaturedYetException(userPseudo))).when(userProfilPictureFeaturedRepository).getFeatured(userPseudo);
        doReturn(Uni.createFrom().item(userProfilePictureIdentifier)).when(userProfilePictureRepository).getLast(userPseudo, supportedMediaType);
        final Response response = mock(Response.class);
        doReturn(response).when(testResponseTransformer).toResponse(userProfilePictureIdentifier);
        doReturn(Uni.createFrom().nullItem()).when(userProfilePictureCacheRepository).get(userPseudo);
        doReturn(Uni.createFrom().nullItem()).when(lockMechanism).lock(userPseudo);
        final InOrder inOrder = inOrder(testResponseTransformer, userProfilePictureRepository, userProfilPictureFeaturedRepository, userProfilePictureCacheRepository, lockMechanism);

        // When
        final UniAssertSubscriber<Response> subscriber = getFeaturedUserProfilePictureUseCase.execute(
                new GetFeaturedUserProfilePictureCommand(userPseudo, supportedMediaType),
                testResponseTransformer)
                .subscribe().withSubscriber(UniAssertSubscriber.create());

        // Then
        subscriber.assertCompleted().assertItem(response);
        inOrder.verify(lockMechanism, times(1)).lock(userPseudo);
        inOrder.verify(userProfilePictureCacheRepository, times(1)).get(any());
        inOrder.verify(userProfilPictureFeaturedRepository, times(1)).getFeatured(any());
        inOrder.verify(userProfilePictureRepository, times(1)).getLast(any(), any());
        inOrder.verify(testResponseTransformer).toResponse(any(UserProfilePictureIdentifier.class));
        inOrder.verify(lockMechanism, times(1)).unlock(userPseudo);
        verifyNoMoreInteractions(testResponseTransformer, userProfilePictureCacheRepository, userProfilPictureFeaturedRepository, userProfilePictureCacheRepository, lockMechanism);
    }

    @Test
    public void should_get_last_user_profile_picture_from_user_profile_picture_repository_when_not_in_cache_and_an_exception_occurs_when_retrieving_it() {
        // Given
        final UserPseudo userPseudo = mock(UserPseudo.class);
        final SupportedMediaType supportedMediaType = mock(SupportedMediaType.class);
        final UserProfilePictureIdentifier userProfilePictureIdentifier = mock(UserProfilePictureIdentifier.class);
        doReturn(Uni.createFrom().nullItem()).when(userProfilePictureCacheRepository).get(userPseudo);
        doReturn(Uni.createFrom().failure(new UserProfilPictureFeaturedRepositoryException())).when(userProfilPictureFeaturedRepository).getFeatured(userPseudo);
        doReturn(Uni.createFrom().item(userProfilePictureIdentifier)).when(userProfilePictureRepository).getLast(userPseudo, supportedMediaType);
        final Response response = mock(Response.class);
        doReturn(response).when(testResponseTransformer).toResponse(userProfilePictureIdentifier);
        doReturn(Uni.createFrom().nullItem()).when(userProfilePictureCacheRepository).get(userPseudo);
        doReturn(Uni.createFrom().nullItem()).when(lockMechanism).lock(userPseudo);
        final InOrder inOrder = inOrder(testResponseTransformer, userProfilePictureRepository, userProfilPictureFeaturedRepository, userProfilePictureCacheRepository, lockMechanism);

        // When
        final UniAssertSubscriber<Response> subscriber = getFeaturedUserProfilePictureUseCase.execute(
                new GetFeaturedUserProfilePictureCommand(userPseudo, supportedMediaType),
                testResponseTransformer)
                .subscribe().withSubscriber(UniAssertSubscriber.create());

        // Then
        subscriber.assertCompleted().assertItem(response);
        inOrder.verify(lockMechanism, times(1)).lock(userPseudo);
        inOrder.verify(userProfilePictureCacheRepository, times(1)).get(any());
        inOrder.verify(userProfilPictureFeaturedRepository, times(1)).getFeatured(any());
        inOrder.verify(userProfilePictureRepository, times(1)).getLast(any(), any());
        inOrder.verify(testResponseTransformer).toResponse(any(UserProfilePictureIdentifier.class));
        inOrder.verify(lockMechanism, times(1)).unlock(userPseudo);
        verifyNoMoreInteractions(testResponseTransformer, userProfilePictureCacheRepository, userProfilPictureFeaturedRepository, userProfilePictureCacheRepository, lockMechanism);
    }

    @Test
    public void should_handle_user_profile_picture_not_available_yet_exception_when_getting_last_user_profile_picture() {
        // Given
        final UserPseudo userPseudo = mock(UserPseudo.class);
        final SupportedMediaType supportedMediaType = mock(SupportedMediaType.class);
        final UserProfilePictureNotAvailableYetException userProfilePictureNotAvailableYetException = mock(UserProfilePictureNotAvailableYetException.class);
        doReturn(Uni.createFrom().nullItem()).when(userProfilePictureCacheRepository).get(userPseudo);
        doReturn(Uni.createFrom().failure(new UserProfilPictureFeaturedRepositoryException())).when(userProfilPictureFeaturedRepository).getFeatured(userPseudo);
        doReturn(Uni.createFrom().failure(userProfilePictureNotAvailableYetException)).when(userProfilePictureRepository).getLast(userPseudo, supportedMediaType);
        final Response response = mock(Response.class);
        doReturn(response).when(testResponseTransformer).toResponse(userProfilePictureNotAvailableYetException);
        doReturn(Uni.createFrom().nullItem()).when(userProfilePictureCacheRepository).get(userPseudo);
        doReturn(Uni.createFrom().nullItem()).when(lockMechanism).lock(userPseudo);
        final InOrder inOrder = inOrder(testResponseTransformer, userProfilePictureRepository, userProfilPictureFeaturedRepository, userProfilePictureCacheRepository, lockMechanism);

        // When
        final UniAssertSubscriber<Response> subscriber = getFeaturedUserProfilePictureUseCase.execute(
                new GetFeaturedUserProfilePictureCommand(userPseudo, supportedMediaType),
                testResponseTransformer)
                .subscribe().withSubscriber(UniAssertSubscriber.create());

        // Then
        subscriber.assertCompleted().assertItem(response);
        inOrder.verify(lockMechanism, times(1)).lock(userPseudo);
        inOrder.verify(userProfilePictureCacheRepository, times(1)).get(any());
        inOrder.verify(userProfilPictureFeaturedRepository, times(1)).getFeatured(any());
        inOrder.verify(userProfilePictureRepository, times(1)).getLast(any(), any());
        inOrder.verify(testResponseTransformer).toResponse(any(UserProfilePictureNotAvailableYetException.class));
        inOrder.verify(lockMechanism, times(1)).unlock(userPseudo);
        verifyNoMoreInteractions(testResponseTransformer, userProfilePictureCacheRepository, userProfilPictureFeaturedRepository, userProfilePictureCacheRepository, lockMechanism);
    }

    @Test
    public void should_handle_user_profile_picture_repository_exception_when_getting_last_user_profile_picture() {
        // Given
        final UserPseudo userPseudo = mock(UserPseudo.class);
        final SupportedMediaType supportedMediaType = mock(SupportedMediaType.class);
        final UserProfilePictureRepositoryException userProfilePictureRepositoryException = mock(UserProfilePictureRepositoryException.class);
        doReturn(Uni.createFrom().nullItem()).when(userProfilePictureCacheRepository).get(userPseudo);
        doReturn(Uni.createFrom().failure(new UserProfilPictureFeaturedRepositoryException())).when(userProfilPictureFeaturedRepository).getFeatured(userPseudo);
        doReturn(Uni.createFrom().failure(userProfilePictureRepositoryException)).when(userProfilePictureRepository).getLast(userPseudo, supportedMediaType);
        final Response response = mock(Response.class);
        doReturn(response).when(testResponseTransformer).toResponse(userProfilePictureRepositoryException);
        doReturn(Uni.createFrom().nullItem()).when(userProfilePictureCacheRepository).get(userPseudo);
        doReturn(Uni.createFrom().nullItem()).when(lockMechanism).lock(userPseudo);
        final InOrder inOrder = inOrder(testResponseTransformer, userProfilePictureRepository, userProfilPictureFeaturedRepository, userProfilePictureCacheRepository, lockMechanism);

        // When
        final UniAssertSubscriber<Response> subscriber = getFeaturedUserProfilePictureUseCase.execute(
                new GetFeaturedUserProfilePictureCommand(userPseudo, supportedMediaType),
                testResponseTransformer)
                .subscribe().withSubscriber(UniAssertSubscriber.create());

        // Then
        subscriber.assertCompleted().assertItem(response);
        inOrder.verify(lockMechanism, times(1)).lock(userPseudo);
        inOrder.verify(userProfilePictureCacheRepository, times(1)).get(any());
        inOrder.verify(userProfilPictureFeaturedRepository, times(1)).getFeatured(any());
        inOrder.verify(userProfilePictureRepository, times(1)).getLast(any(), any());
        inOrder.verify(testResponseTransformer).toResponse(any(UserProfilePictureRepositoryException.class));
        inOrder.verify(lockMechanism, times(1)).unlock(userPseudo);
        verifyNoMoreInteractions(testResponseTransformer, userProfilePictureCacheRepository, userProfilPictureFeaturedRepository, userProfilePictureCacheRepository, lockMechanism);
    }

}
