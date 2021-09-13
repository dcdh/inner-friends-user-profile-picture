package com.innerfriends.userprofilepicture.domain.usecase;

import com.innerfriends.userprofilepicture.domain.*;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.helpers.test.UniAssertSubscriber;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class ListUserProfilPicturesUseCaseTest {

    private UserProfilePictureRepository userProfilePictureRepository;
    private ListUserProfilPicturesUseCase<Response> listUserProfilPicturesUseCase;
    private TestResponseTransformer testResponseTransformer;
    private UserProfilePictureCacheRepository userProfilePictureCacheRepository;
    private LockMechanism lockMechanism;

    @BeforeEach
    public void setup() {
        userProfilePictureRepository = mock(UserProfilePictureRepository.class);
        userProfilePictureCacheRepository = mock(UserProfilePictureCacheRepository.class);
        lockMechanism = mock(LockMechanism.class);
        listUserProfilPicturesUseCase = new ListUserProfilPicturesUseCase<>(userProfilePictureRepository, userProfilePictureCacheRepository, lockMechanism);
        testResponseTransformer = mock(TestResponseTransformer.class);
    }

    @Test
    public void should_get_featured_user_profile_picture_from_cache_when_present() {
        // Given
        final UserPseudo userPseudo = mock(UserPseudo.class);
        final SupportedMediaType supportedMediaType = mock(SupportedMediaType.class);
        final CachedUserProfilePictures cachedUserProfilePictures = mock(CachedUserProfilePictures.class);
        final List<UserProfilePictureIdentifier> userProfilePictureIdentifiers = Collections.emptyList();
        doReturn(true).when(cachedUserProfilePictures).hasUserProfilePictureIdentifiersInCache();
        doReturn(userProfilePictureIdentifiers).when(cachedUserProfilePictures).userProfilePictureIdentifiers();
        doReturn(Uni.createFrom().item(cachedUserProfilePictures)).when(userProfilePictureCacheRepository).get(userPseudo);
        doReturn(Uni.createFrom().nullItem()).when(lockMechanism).lock(userPseudo);
        final Response response = mock(Response.class);
        doReturn(response).when(testResponseTransformer).toResponse(userProfilePictureIdentifiers);

        // When
        final UniAssertSubscriber<Response> subscriber = listUserProfilPicturesUseCase.execute(
                new ListUserProfilPicturesCommand(userPseudo, supportedMediaType),
                testResponseTransformer)
                .subscribe().withSubscriber(UniAssertSubscriber.create());

        // Then
        subscriber.assertCompleted().assertItem(response);
        verify(lockMechanism, times(1)).lock(userPseudo);
        verify(userProfilePictureCacheRepository, times(1)).get(any());
        verify(cachedUserProfilePictures, times(1)).hasUserProfilePictureIdentifiersInCache();
        verify(cachedUserProfilePictures, times(1)).userProfilePictureIdentifiers();
        verify(testResponseTransformer).toResponse(any(List.class));
        verify(lockMechanism, times(1)).unlock(userPseudo);
        verifyNoMoreInteractions(testResponseTransformer, userProfilePictureCacheRepository, userProfilePictureCacheRepository, lockMechanism);
    }

    @Test
    public void should_get_featured_user_profile_picture_and_cache_it_when_not_in_cache() {
        // Given
        final UserPseudo userPseudo = mock(UserPseudo.class);
        final SupportedMediaType supportedMediaType = mock(SupportedMediaType.class);
        final List<UserProfilePictureIdentifier> userProfilePictureIdentifiers = Collections.emptyList();
        doReturn(Uni.createFrom().nullItem()).when(userProfilePictureCacheRepository).get(userPseudo);
        doReturn(Uni.createFrom().item(userProfilePictureIdentifiers)).when(userProfilePictureRepository).listByUserPseudo(userPseudo, supportedMediaType);
        doReturn(Uni.createFrom().nullItem()).when(userProfilePictureCacheRepository).store(userPseudo, userProfilePictureIdentifiers);
        doReturn(Uni.createFrom().nullItem()).when(lockMechanism).lock(userPseudo);

        final Response response = mock(Response.class);
        doReturn(response).when(testResponseTransformer).toResponse(userProfilePictureIdentifiers);
        final InOrder inOrder = inOrder(testResponseTransformer, userProfilePictureRepository, userProfilePictureCacheRepository, lockMechanism);

        // When
        final UniAssertSubscriber<Response> subscriber = listUserProfilPicturesUseCase.execute(
                new ListUserProfilPicturesCommand(userPseudo, supportedMediaType),
                testResponseTransformer)
                .subscribe().withSubscriber(UniAssertSubscriber.create());

        // Then
        subscriber.assertCompleted().assertItem(response);
        inOrder.verify(lockMechanism, times(1)).lock(any());
        inOrder.verify(userProfilePictureCacheRepository, times(1)).get(any());
        inOrder.verify(userProfilePictureRepository, times(1)).listByUserPseudo(any(), any());
        inOrder.verify(userProfilePictureCacheRepository, times(1)).store(any(), any(List.class));
        inOrder.verify(testResponseTransformer).toResponse(any(List.class));
        inOrder.verify(lockMechanism, times(1)).unlock(any());
        verifyNoMoreInteractions(testResponseTransformer, userProfilePictureCacheRepository, userProfilePictureCacheRepository, lockMechanism);
    }

    @Test
    public void should_get_featured_user_profile_picture_and_cache_it_when_profile_pictures_identifiers_not_in_cache() {
        // Given
        final UserPseudo userPseudo = mock(UserPseudo.class);
        final SupportedMediaType supportedMediaType = mock(SupportedMediaType.class);
        final CachedUserProfilePictures cachedUserProfilePictures = mock(CachedUserProfilePictures.class);
        final List<UserProfilePictureIdentifier> userProfilePictureIdentifiers = Collections.emptyList();
        doReturn(false).when(cachedUserProfilePictures).hasUserProfilePictureIdentifiersInCache();
        doReturn(userProfilePictureIdentifiers).when(cachedUserProfilePictures).userProfilePictureIdentifiers();
        doReturn(Uni.createFrom().item(cachedUserProfilePictures)).when(userProfilePictureCacheRepository).get(userPseudo);
        doReturn(Uni.createFrom().item(userProfilePictureIdentifiers)).when(userProfilePictureRepository).listByUserPseudo(userPseudo, supportedMediaType);
        doReturn(Uni.createFrom().nullItem()).when(userProfilePictureCacheRepository).store(userPseudo, userProfilePictureIdentifiers);
        doReturn(Uni.createFrom().nullItem()).when(lockMechanism).lock(userPseudo);

        final Response response = mock(Response.class);
        doReturn(response).when(testResponseTransformer).toResponse(userProfilePictureIdentifiers);
        final InOrder inOrder = inOrder(testResponseTransformer, userProfilePictureRepository, userProfilePictureCacheRepository, lockMechanism);

        // When
        final UniAssertSubscriber<Response> subscriber = listUserProfilPicturesUseCase.execute(
                new ListUserProfilPicturesCommand(userPseudo, supportedMediaType),
                testResponseTransformer)
                .subscribe().withSubscriber(UniAssertSubscriber.create());

        // Then
        subscriber.assertCompleted().assertItem(response);
        verify(cachedUserProfilePictures, times(1)).hasUserProfilePictureIdentifiersInCache();
        inOrder.verify(lockMechanism, times(1)).lock(any());
        inOrder.verify(userProfilePictureCacheRepository, times(1)).get(any());
        inOrder.verify(userProfilePictureRepository, times(1)).listByUserPseudo(any(), any());
        inOrder.verify(userProfilePictureCacheRepository, times(1)).store(any(), any(List.class));
        inOrder.verify(testResponseTransformer).toResponse(any(List.class));
        inOrder.verify(lockMechanism, times(1)).unlock(any());
        verifyNoMoreInteractions(testResponseTransformer, userProfilePictureCacheRepository, userProfilePictureCacheRepository, lockMechanism);
    }

    @Test
    public void should_handle_profile_picture_repository_exception() {
        // Given
        doReturn(Uni.createFrom().nullItem()).when(userProfilePictureCacheRepository).get(any());
        final UserProfilePictureRepositoryException userProfilePictureRepositoryException = mock(UserProfilePictureRepositoryException.class);
        doReturn(Uni.createFrom().failure(userProfilePictureRepositoryException)).when(userProfilePictureRepository).listByUserPseudo(any(), any());
        final Response response = mock(Response.class);
        doReturn(response).when(testResponseTransformer).toResponse(userProfilePictureRepositoryException);
        doReturn(Uni.createFrom().nullItem()).when(lockMechanism).lock(any());

        // When
        final UniAssertSubscriber<Response> subscriber = listUserProfilPicturesUseCase.execute(
                new ListUserProfilPicturesCommand(mock(UserPseudo.class), mock(SupportedMediaType.class)),
                testResponseTransformer)
                .subscribe().withSubscriber(UniAssertSubscriber.create());

        // Then
        subscriber.assertCompleted().assertItem(response);
        verify(lockMechanism, times(1)).lock(any());
        verify(userProfilePictureCacheRepository, times(1)).get(any());
        verify(userProfilePictureRepository, times(1)).listByUserPseudo(any(), any());
        verify(testResponseTransformer).toResponse(any(UserProfilePictureRepositoryException.class));
        verify(lockMechanism, times(1)).unlock(any());
        verifyNoMoreInteractions(testResponseTransformer, userProfilePictureCacheRepository, userProfilePictureCacheRepository, lockMechanism);
    }

    @Test
    public void should_handle_runtime_exception() {
        // Given
        doReturn(Uni.createFrom().nullItem()).when(userProfilePictureCacheRepository).get(any());
        final RuntimeException runtimeException = new RuntimeException();
        doReturn(Uni.createFrom().failure(runtimeException)).when(userProfilePictureRepository).listByUserPseudo(any(), any());
        doReturn(Uni.createFrom().nullItem()).when(lockMechanism).lock(any());
        final Response response = mock(Response.class);
        doReturn(response).when(testResponseTransformer).toResponse(runtimeException);

        // When
        final UniAssertSubscriber<Response> subscriber = listUserProfilPicturesUseCase.execute(
                new ListUserProfilPicturesCommand(mock(UserPseudo.class), mock(SupportedMediaType.class)),
                testResponseTransformer)
                .subscribe().withSubscriber(UniAssertSubscriber.create());

        // Then
        subscriber.assertCompleted().assertItem(response);
        verify(lockMechanism, times(1)).lock(any());
        verify(userProfilePictureCacheRepository, times(1)).get(any());
        verify(userProfilePictureRepository, times(1)).listByUserPseudo(any(), any());
        verify(testResponseTransformer).toResponse(any(RuntimeException.class));
        verify(lockMechanism, times(1)).unlock(any());
        verifyNoMoreInteractions(testResponseTransformer, userProfilePictureCacheRepository, userProfilePictureCacheRepository, lockMechanism);
    }

    @Test
    public void should_get_featured_user_profile_picture_when_cache_is_down() {
        // Given
        final UserPseudo userPseudo = mock(UserPseudo.class);
        final SupportedMediaType supportedMediaType = mock(SupportedMediaType.class);
        final List<UserProfilePictureIdentifier> userProfilePictureIdentifiers = Collections.emptyList();
        doReturn(Uni.createFrom().failure(RuntimeException::new)).when(userProfilePictureCacheRepository).get(userPseudo);
        doReturn(Uni.createFrom().item(userProfilePictureIdentifiers)).when(userProfilePictureRepository).listByUserPseudo(userPseudo, supportedMediaType);
        doReturn(Uni.createFrom().failure(RuntimeException::new)).when(userProfilePictureCacheRepository).store(userPseudo, userProfilePictureIdentifiers);
        doReturn(Uni.createFrom().nullItem()).when(lockMechanism).lock(userPseudo);

        final Response response = mock(Response.class);
        doReturn(response).when(testResponseTransformer).toResponse(userProfilePictureIdentifiers);
        final InOrder inOrder = inOrder(testResponseTransformer, userProfilePictureRepository, userProfilePictureCacheRepository, lockMechanism);

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
        inOrder.verify(userProfilePictureCacheRepository, times(1)).store(any(), any(List.class));
        inOrder.verify(testResponseTransformer).toResponse(any(List.class));
        inOrder.verify(lockMechanism, times(1)).unlock(userPseudo);
        verifyNoMoreInteractions(testResponseTransformer, userProfilePictureCacheRepository, userProfilePictureCacheRepository, lockMechanism);
    }

}
