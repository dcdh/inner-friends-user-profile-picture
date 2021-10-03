package com.innerfriends.userprofilepicture.domain.usecase;

import com.innerfriends.userprofilepicture.domain.*;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.helpers.test.UniAssertSubscriber;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class ListUserProfilPicturesUseCaseTest {

    private UserProfilePictureRepository userProfilePictureRepository;
    private ListUserProfilPicturesUseCase<Response> listUserProfilPicturesUseCase;
    private TestResponseTransformer testResponseTransformer;
    private UserProfilePictureCacheRepository userProfilePictureCacheRepository;
    private LockMechanism lockMechanism;
    private UserProfilPictureFeaturedRepository userProfilPictureFeaturedRepository;

    @BeforeEach
    public void setup() {
        userProfilePictureRepository = mock(UserProfilePictureRepository.class);
        userProfilePictureCacheRepository = mock(UserProfilePictureCacheRepository.class);
        lockMechanism = mock(LockMechanism.class);
        userProfilPictureFeaturedRepository = mock(UserProfilPictureFeaturedRepository.class);
        listUserProfilPicturesUseCase = new ListUserProfilPicturesUseCase<>(userProfilePictureRepository, userProfilePictureCacheRepository, lockMechanism, userProfilPictureFeaturedRepository);
        testResponseTransformer = mock(TestResponseTransformer.class);
    }

    @Test
    public void should_list_user_profile_pictures_from_cache_when_present() {
        // Given
        final UserPseudo userPseudo = mock(UserPseudo.class);
        final SupportedMediaType supportedMediaType = mock(SupportedMediaType.class);
        final CachedUserProfilePictures cachedUserProfilePictures = mock(CachedUserProfilePictures.class);
        doReturn(true).when(cachedUserProfilePictures).hasUserProfilePictureIdentifiersInCache();
        doReturn(Uni.createFrom().item(cachedUserProfilePictures)).when(userProfilePictureCacheRepository).get(userPseudo);
        doReturn(Uni.createFrom().nullItem()).when(lockMechanism).lock(userPseudo);
        final Response response = mock(Response.class);
        doReturn(response).when(testResponseTransformer).toResponse(cachedUserProfilePictures);
        final InOrder inOrder = inOrder(testResponseTransformer, cachedUserProfilePictures, userProfilePictureRepository, userProfilePictureCacheRepository, lockMechanism);

        // When
        final UniAssertSubscriber<Response> subscriber = listUserProfilPicturesUseCase.execute(
                new ListUserProfilPicturesCommand(userPseudo, supportedMediaType),
                testResponseTransformer)
                .subscribe().withSubscriber(UniAssertSubscriber.create());

        // Then
        subscriber.assertCompleted().assertItem(response);
        inOrder.verify(lockMechanism, times(1)).lock(userPseudo);
        inOrder.verify(userProfilePictureCacheRepository, times(1)).get(any());
        inOrder.verify(cachedUserProfilePictures, times(1)).hasUserProfilePictureIdentifiersInCache();
        inOrder.verify(testResponseTransformer).toResponse(any(CachedUserProfilePictures.class));
        inOrder.verify(lockMechanism, times(1)).unlock(userPseudo);
        verifyNoMoreInteractions(testResponseTransformer, cachedUserProfilePictures, userProfilePictureRepository, userProfilePictureCacheRepository, lockMechanism);
    }

    @Test
    public void should_list_user_profile_pictures_and_cache_it_when_not_in_cache() {
        // Given
        final UserPseudo userPseudo = mock(UserPseudo.class);
        final SupportedMediaType supportedMediaType = mock(SupportedMediaType.class);
        final CachedUserProfilePictures cachedUserProfilePictures = mock(CachedUserProfilePictures.class);
        doReturn(false).when(cachedUserProfilePictures).hasUserProfilePictureIdentifiersInCache();
        doReturn(Uni.createFrom().item(cachedUserProfilePictures)).when(userProfilePictureCacheRepository).get(userPseudo);
        doReturn(Uni.createFrom().nullItem()).when(lockMechanism).lock(userPseudo);
        doReturn(Uni.createFrom().item(Collections.emptyList())).when(userProfilePictureRepository).listByUserPseudo(userPseudo, supportedMediaType);
        final UserProfilePictureIdentifier userProfilePictureIdentifier = mock(UserProfilePictureIdentifier.class);
        doReturn(Uni.createFrom().item(userProfilePictureIdentifier)).when(userProfilPictureFeaturedRepository).getFeatured(userPseudo);
        final UserProfilePictures userProfilePictures = DomainUserProfilePictures.newBuilder()
                .withFeaturedStateSelected(Collections.emptyList(), userProfilePictureIdentifier).build();
        doReturn(Uni.createFrom().item(cachedUserProfilePictures)).when(userProfilePictureCacheRepository).store(userPseudo, userProfilePictures);
        final Response response = mock(Response.class);
        doReturn(response).when(testResponseTransformer).toResponse(userProfilePictures);
        final InOrder inOrder = inOrder(testResponseTransformer, cachedUserProfilePictures, userProfilePictureRepository, userProfilePictureCacheRepository,
                userProfilPictureFeaturedRepository, lockMechanism);

        // When
        final UniAssertSubscriber<Response> subscriber = listUserProfilPicturesUseCase.execute(
                new ListUserProfilPicturesCommand(userPseudo, supportedMediaType),
                testResponseTransformer)
                .subscribe().withSubscriber(UniAssertSubscriber.create());

        // Then
        subscriber.assertCompleted().assertItem(response);
        inOrder.verify(lockMechanism, times(1)).lock(userPseudo);
        inOrder.verify(userProfilePictureCacheRepository, times(1)).get(any());
        inOrder.verify(cachedUserProfilePictures, times(1)).hasUserProfilePictureIdentifiersInCache();
        inOrder.verify(userProfilePictureRepository, times(1)).listByUserPseudo(any(), any());
        inOrder.verify(userProfilPictureFeaturedRepository, times(1)).getFeatured(any());
        inOrder.verify(userProfilePictureCacheRepository, times(1)).store(any(), any());
        inOrder.verify(testResponseTransformer).toResponse(any(DomainUserProfilePictures.class));
        inOrder.verify(lockMechanism, times(1)).unlock(userPseudo);
        verifyNoMoreInteractions(testResponseTransformer, cachedUserProfilePictures, userProfilePictureRepository, userProfilePictureCacheRepository,
                userProfilPictureFeaturedRepository, lockMechanism);
    }

    @Test
    public void should_handle_no_user_profile_picture_yet_when_getting_featured_user_picture() {
        // Given
        final UserPseudo userPseudo = mock(UserPseudo.class);
        final SupportedMediaType supportedMediaType = mock(SupportedMediaType.class);
        final CachedUserProfilePictures cachedUserProfilePictures = mock(CachedUserProfilePictures.class);
        doReturn(false).when(cachedUserProfilePictures).hasUserProfilePictureIdentifiersInCache();
        doReturn(Uni.createFrom().item(cachedUserProfilePictures)).when(userProfilePictureCacheRepository).get(userPseudo);
        doReturn(Uni.createFrom().nullItem()).when(lockMechanism).lock(userPseudo);
        doReturn(Uni.createFrom().item(Collections.emptyList())).when(userProfilePictureRepository).listByUserPseudo(userPseudo, supportedMediaType);
        doReturn(Uni.createFrom().failure(new NoUserProfilPictureFeaturedYetException(userPseudo))).when(userProfilPictureFeaturedRepository).getFeatured(userPseudo);
        final UserProfilePictures userProfilePictures = DomainUserProfilePictures.newBuilder()
                .withFeaturedStateNotSelectedYet(Collections.emptyList()).build();
        doReturn(Uni.createFrom().item(cachedUserProfilePictures)).when(userProfilePictureCacheRepository).store(userPseudo, userProfilePictures);
        final Response response = mock(Response.class);
        doReturn(response).when(testResponseTransformer).toResponse(userProfilePictures);
        final InOrder inOrder = inOrder(testResponseTransformer, cachedUserProfilePictures, userProfilePictureRepository, userProfilePictureCacheRepository,
                userProfilPictureFeaturedRepository, lockMechanism);

        // When
        final UniAssertSubscriber<Response> subscriber = listUserProfilPicturesUseCase.execute(
                new ListUserProfilPicturesCommand(userPseudo, supportedMediaType),
                testResponseTransformer)
                .subscribe().withSubscriber(UniAssertSubscriber.create());

        // Then
        subscriber.assertCompleted().assertItem(response);
        inOrder.verify(lockMechanism, times(1)).lock(userPseudo);
        inOrder.verify(userProfilePictureCacheRepository, times(1)).get(any());
        inOrder.verify(cachedUserProfilePictures, times(1)).hasUserProfilePictureIdentifiersInCache();
        inOrder.verify(userProfilePictureRepository, times(1)).listByUserPseudo(any(), any());
        inOrder.verify(userProfilPictureFeaturedRepository, times(1)).getFeatured(any());
        inOrder.verify(userProfilePictureCacheRepository, times(1)).store(any(), any());
        inOrder.verify(testResponseTransformer).toResponse(any(DomainUserProfilePictures.class));
        inOrder.verify(lockMechanism, times(1)).unlock(userPseudo);
        verifyNoMoreInteractions(testResponseTransformer, cachedUserProfilePictures, userProfilePictureRepository, userProfilePictureCacheRepository,
                userProfilPictureFeaturedRepository, lockMechanism);
    }

    @Test
    public void should_handle_runtime_exception_when_getting_featured_user_picture() {
        // Given
        final UserPseudo userPseudo = mock(UserPseudo.class);
        final SupportedMediaType supportedMediaType = mock(SupportedMediaType.class);
        final CachedUserProfilePictures cachedUserProfilePictures = mock(CachedUserProfilePictures.class);
        doReturn(false).when(cachedUserProfilePictures).hasUserProfilePictureIdentifiersInCache();
        doReturn(Uni.createFrom().item(cachedUserProfilePictures)).when(userProfilePictureCacheRepository).get(userPseudo);
        doReturn(Uni.createFrom().nullItem()).when(lockMechanism).lock(userPseudo);
        doReturn(Uni.createFrom().item(Collections.emptyList())).when(userProfilePictureRepository).listByUserPseudo(userPseudo, supportedMediaType);
        doReturn(Uni.createFrom().failure(new RuntimeException())).when(userProfilPictureFeaturedRepository).getFeatured(userPseudo);
        final UserProfilePictures userProfilePictures = DomainUserProfilePictures.newBuilder()
                .withFeaturedStateInErrorWhenRetrieving(Collections.emptyList()).build();
        doReturn(Uni.createFrom().item(cachedUserProfilePictures)).when(userProfilePictureCacheRepository).store(userPseudo, userProfilePictures);
        final Response response = mock(Response.class);
        doReturn(response).when(testResponseTransformer).toResponse(userProfilePictures);
        final InOrder inOrder = inOrder(testResponseTransformer, cachedUserProfilePictures, userProfilePictureRepository, userProfilePictureCacheRepository,
                userProfilPictureFeaturedRepository, lockMechanism);

        // When
        final UniAssertSubscriber<Response> subscriber = listUserProfilPicturesUseCase.execute(
                new ListUserProfilPicturesCommand(userPseudo, supportedMediaType),
                testResponseTransformer)
                .subscribe().withSubscriber(UniAssertSubscriber.create());

        // Then
        subscriber.assertCompleted().assertItem(response);
        inOrder.verify(lockMechanism, times(1)).lock(userPseudo);
        inOrder.verify(userProfilePictureCacheRepository, times(1)).get(any());
        inOrder.verify(cachedUserProfilePictures, times(1)).hasUserProfilePictureIdentifiersInCache();
        inOrder.verify(userProfilePictureRepository, times(1)).listByUserPseudo(any(), any());
        inOrder.verify(userProfilPictureFeaturedRepository, times(1)).getFeatured(any());
        inOrder.verify(userProfilePictureCacheRepository, times(1)).store(any(), any());
        inOrder.verify(testResponseTransformer).toResponse(any(DomainUserProfilePictures.class));
        inOrder.verify(lockMechanism, times(1)).unlock(userPseudo);
        verifyNoMoreInteractions(testResponseTransformer, cachedUserProfilePictures, userProfilePictureRepository, userProfilePictureCacheRepository,
                userProfilPictureFeaturedRepository, lockMechanism);
    }

    @Test
    public void should_handle_user_profile_picture_repository_exception_when_listing_by_user_pseudo() {
        // Given
        final UserPseudo userPseudo = mock(UserPseudo.class);
        final SupportedMediaType supportedMediaType = mock(SupportedMediaType.class);
        final CachedUserProfilePictures cachedUserProfilePictures = mock(CachedUserProfilePictures.class);
        doReturn(false).when(cachedUserProfilePictures).hasUserProfilePictureIdentifiersInCache();
        doReturn(Uni.createFrom().item(cachedUserProfilePictures)).when(userProfilePictureCacheRepository).get(userPseudo);
        doReturn(Uni.createFrom().nullItem()).when(lockMechanism).lock(userPseudo);
        final UserProfilePictureRepositoryException userProfilePictureRepositoryException = mock(UserProfilePictureRepositoryException.class);
        doReturn(Uni.createFrom().failure(userProfilePictureRepositoryException)).when(userProfilePictureRepository).listByUserPseudo(userPseudo, supportedMediaType);
        final Response response = mock(Response.class);
        doReturn(response).when(testResponseTransformer).toResponse(userProfilePictureRepositoryException);
        final InOrder inOrder = inOrder(testResponseTransformer, cachedUserProfilePictures, userProfilePictureRepository, userProfilePictureCacheRepository,
                userProfilPictureFeaturedRepository, lockMechanism);

        // When
        final UniAssertSubscriber<Response> subscriber = listUserProfilPicturesUseCase.execute(
                new ListUserProfilPicturesCommand(userPseudo, supportedMediaType),
                testResponseTransformer)
                .subscribe().withSubscriber(UniAssertSubscriber.create());

        // Then
        subscriber.assertCompleted().assertItem(response);
        inOrder.verify(lockMechanism, times(1)).lock(userPseudo);
        inOrder.verify(userProfilePictureCacheRepository, times(1)).get(any());
        inOrder.verify(cachedUserProfilePictures, times(1)).hasUserProfilePictureIdentifiersInCache();
        inOrder.verify(userProfilePictureRepository, times(1)).listByUserPseudo(any(), any());
        inOrder.verify(testResponseTransformer).toResponse(any(UserProfilePictureRepositoryException.class));
        inOrder.verify(lockMechanism, times(1)).unlock(userPseudo);
        verifyNoMoreInteractions(testResponseTransformer, cachedUserProfilePictures, userProfilePictureRepository, userProfilePictureCacheRepository,
                userProfilPictureFeaturedRepository, lockMechanism);
    }

    @Test
    public void should_handle_runtime_exception_when_listing_by_user_pseudo() {
        // Given
        final UserPseudo userPseudo = mock(UserPseudo.class);
        final SupportedMediaType supportedMediaType = mock(SupportedMediaType.class);
        final CachedUserProfilePictures cachedUserProfilePictures = mock(CachedUserProfilePictures.class);
        doReturn(false).when(cachedUserProfilePictures).hasUserProfilePictureIdentifiersInCache();
        doReturn(Uni.createFrom().item(cachedUserProfilePictures)).when(userProfilePictureCacheRepository).get(userPseudo);
        doReturn(Uni.createFrom().nullItem()).when(lockMechanism).lock(userPseudo);
        final RuntimeException runtimeException = mock(RuntimeException.class);
        doReturn(Uni.createFrom().failure(runtimeException)).when(userProfilePictureRepository).listByUserPseudo(userPseudo, supportedMediaType);
        final Response response = mock(Response.class);
        doReturn(response).when(testResponseTransformer).toResponse(runtimeException);
        final InOrder inOrder = inOrder(testResponseTransformer, cachedUserProfilePictures, userProfilePictureRepository, userProfilePictureCacheRepository,
                userProfilPictureFeaturedRepository, lockMechanism);

        // When
        final UniAssertSubscriber<Response> subscriber = listUserProfilPicturesUseCase.execute(
                new ListUserProfilPicturesCommand(userPseudo, supportedMediaType),
                testResponseTransformer)
                .subscribe().withSubscriber(UniAssertSubscriber.create());

        // Then
        subscriber.assertCompleted().assertItem(response);
        inOrder.verify(lockMechanism, times(1)).lock(userPseudo);
        inOrder.verify(userProfilePictureCacheRepository, times(1)).get(any());
        inOrder.verify(cachedUserProfilePictures, times(1)).hasUserProfilePictureIdentifiersInCache();
        inOrder.verify(userProfilePictureRepository, times(1)).listByUserPseudo(any(), any());
        inOrder.verify(testResponseTransformer).toResponse(any(RuntimeException.class));
        inOrder.verify(lockMechanism, times(1)).unlock(userPseudo);
        verifyNoMoreInteractions(testResponseTransformer, cachedUserProfilePictures, userProfilePictureRepository, userProfilePictureCacheRepository,
                userProfilPictureFeaturedRepository, lockMechanism);
    }

    @Test
    public void should_list_user_profile_pictures_and_cache_it_when_cache_is_down() {
        // Given
        final UserPseudo userPseudo = mock(UserPseudo.class);
        final SupportedMediaType supportedMediaType = mock(SupportedMediaType.class);
        doReturn(Uni.createFrom().failure(RuntimeException::new)).when(userProfilePictureCacheRepository).get(userPseudo);
        doReturn(Uni.createFrom().nullItem()).when(lockMechanism).lock(userPseudo);
        doReturn(Uni.createFrom().item(Collections.emptyList())).when(userProfilePictureRepository).listByUserPseudo(userPseudo, supportedMediaType);
        final UserProfilePictureIdentifier userProfilePictureIdentifier = mock(UserProfilePictureIdentifier.class);
        doReturn(Uni.createFrom().item(userProfilePictureIdentifier)).when(userProfilPictureFeaturedRepository).getFeatured(userPseudo);
        final UserProfilePictures userProfilePictures = DomainUserProfilePictures.newBuilder()
                .withFeaturedStateSelected(Collections.emptyList(), userProfilePictureIdentifier).build();
        doReturn(Uni.createFrom().failure(RuntimeException::new)).when(userProfilePictureCacheRepository).store(userPseudo, userProfilePictures);
        final Response response = mock(Response.class);
        doReturn(response).when(testResponseTransformer).toResponse(userProfilePictures);
        final InOrder inOrder = inOrder(testResponseTransformer, userProfilePictureRepository, userProfilePictureCacheRepository,
                userProfilPictureFeaturedRepository, lockMechanism);

        // When
        final UniAssertSubscriber<Response> subscriber = listUserProfilPicturesUseCase.execute(
                new ListUserProfilPicturesCommand(userPseudo, supportedMediaType),
                testResponseTransformer)
                .subscribe().withSubscriber(UniAssertSubscriber.create());

        // Then
        subscriber.assertCompleted().assertItem(response);
        inOrder.verify(lockMechanism, times(1)).lock(userPseudo);
        inOrder.verify(userProfilePictureCacheRepository, times(1)).get(any());
        inOrder.verify(userProfilePictureRepository, times(1)).listByUserPseudo(any(), any());
        inOrder.verify(userProfilPictureFeaturedRepository, times(1)).getFeatured(any());
        inOrder.verify(userProfilePictureCacheRepository, times(1)).store(any(), any());
        inOrder.verify(testResponseTransformer).toResponse(any(DomainUserProfilePictures.class));
        inOrder.verify(lockMechanism, times(1)).unlock(userPseudo);
        verifyNoMoreInteractions(testResponseTransformer, userProfilePictureRepository, userProfilePictureCacheRepository,
                userProfilPictureFeaturedRepository, lockMechanism);
    }

}
