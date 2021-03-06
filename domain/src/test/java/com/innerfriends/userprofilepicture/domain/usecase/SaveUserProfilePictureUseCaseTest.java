package com.innerfriends.userprofilepicture.domain.usecase;

import com.innerfriends.userprofilepicture.domain.*;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.helpers.test.UniAssertSubscriber;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import static org.mockito.Mockito.*;

public class SaveUserProfilePictureUseCaseTest {

    private UserProfilePictureRepository userProfilePictureRepository;
    private SaveUserProfilePictureUseCase<Response> saveUserProfilePictureUseCase;
    private TestResponseTransformer testResponseTransformer;
    private UserProfilePictureCacheRepository userProfilePictureCacheRepository;
    private LockMechanism lockMechanism;

    @BeforeEach
    public void setup() {
        userProfilePictureRepository = mock(UserProfilePictureRepository.class);
        userProfilePictureCacheRepository = mock(UserProfilePictureCacheRepository.class);
        lockMechanism = mock(LockMechanism.class);
        saveUserProfilePictureUseCase = new SaveUserProfilePictureUseCase<>(userProfilePictureRepository, userProfilePictureCacheRepository, lockMechanism);
        testResponseTransformer = mock(TestResponseTransformer.class);
    }

    @Test
    public void should_save_user_profile_picture_and_cache_it() {
        // Given
        final UserPseudo userPseudo = mock(UserPseudo.class);
        final UserProfilePictureSaved profilePictureSaved = mock(UserProfilePictureSaved.class);
        doReturn(Uni.createFrom().item(profilePictureSaved)).when(userProfilePictureRepository).save(userPseudo, "content".getBytes(), SupportedMediaType.IMAGE_JPEG);
        doReturn(Uni.createFrom().nullItem()).when(userProfilePictureCacheRepository).evict(userPseudo);
        doReturn(Uni.createFrom().nullItem()).when(lockMechanism).lock(userPseudo);
        final Response response = mock(Response.class);
        doReturn(response).when(testResponseTransformer).toResponse(profilePictureSaved);
        final InOrder inOrder = inOrder(testResponseTransformer, userProfilePictureRepository, userProfilePictureCacheRepository, lockMechanism);

        // When
        final UniAssertSubscriber<Response> subscriber = saveUserProfilePictureUseCase.execute(
                new SaveUserProfilePictureCommand(userPseudo, "content".getBytes(), SupportedMediaType.IMAGE_JPEG),
                testResponseTransformer)
                .subscribe().withSubscriber(UniAssertSubscriber.create());

        // Then
        subscriber.assertCompleted().assertItem(response);
        inOrder.verify(lockMechanism, times(1)).lock(userPseudo);
        inOrder.verify(userProfilePictureRepository, times(1)).save(any(), any(), any());
        inOrder.verify(userProfilePictureCacheRepository, times(1)).evict(any());
        inOrder.verify(testResponseTransformer).toResponse(any(UserProfilePictureSaved.class));
        inOrder.verify(lockMechanism, times(1)).unlock(userPseudo);
        verifyNoMoreInteractions(testResponseTransformer, userProfilePictureCacheRepository, userProfilePictureCacheRepository, lockMechanism);
    }

    @Test
    public void should_handle_user_profile_picture_repository_exception() {
        // Given
        final UserProfilePictureRepositoryException userProfilePictureRepositoryException = mock(UserProfilePictureRepositoryException.class);
        doReturn(Uni.createFrom().failure(userProfilePictureRepositoryException)).when(userProfilePictureRepository).save(any(), any(), any());
        doReturn(Uni.createFrom().nullItem()).when(lockMechanism).lock(any());
        final Response response = mock(Response.class);
        doReturn(response).when(testResponseTransformer).toResponse(userProfilePictureRepositoryException);

        // When
        final UniAssertSubscriber<Response> subscriber = saveUserProfilePictureUseCase.execute(
                new SaveUserProfilePictureCommand(mock(UserPseudo.class), "content".getBytes(), SupportedMediaType.IMAGE_JPEG),
                testResponseTransformer)
                .subscribe().withSubscriber(UniAssertSubscriber.create());

        // Then
        subscriber.assertCompleted().assertItem(response);
        verify(lockMechanism, times(1)).lock(any());
        verify(userProfilePictureRepository, times(1)).save(any(), any(), any());
        verify(testResponseTransformer).toResponse(any(UserProfilePictureRepositoryException.class));
        verify(lockMechanism, times(1)).unlock(any());
        verifyNoMoreInteractions(testResponseTransformer, userProfilePictureCacheRepository, userProfilePictureCacheRepository, lockMechanism);
    }

    @Test
    public void should_handle_runtime_exception() {
        // Given
        final RuntimeException runtimeException = new RuntimeException();
        doReturn(Uni.createFrom().failure(runtimeException)).when(userProfilePictureRepository).save(any(), any(), any());
        doReturn(Uni.createFrom().nullItem()).when(lockMechanism).lock(any());
        final Response response = mock(Response.class);
        doReturn(response).when(testResponseTransformer).toResponse(runtimeException);

        // When
        final UniAssertSubscriber<Response> subscriber = saveUserProfilePictureUseCase.execute(
                new SaveUserProfilePictureCommand(mock(UserPseudo.class), "content".getBytes(), SupportedMediaType.IMAGE_JPEG),
                testResponseTransformer)
                .subscribe().withSubscriber(UniAssertSubscriber.create());

        // Then
        subscriber.assertCompleted().assertItem(response);
        verify(lockMechanism, times(1)).lock(any());
        verify(userProfilePictureRepository, times(1)).save(any(), any(), any());
        verify(testResponseTransformer).toResponse(any(RuntimeException.class));
        verify(lockMechanism, times(1)).unlock(any());
        verifyNoMoreInteractions(testResponseTransformer, userProfilePictureCacheRepository, userProfilePictureCacheRepository, lockMechanism);
    }

    @Test
    public void should_save_user_profile_picture_when_cache_id_down() {
        // Given
        final UserPseudo userPseudo = mock(UserPseudo.class);
        final UserProfilePictureSaved profilePictureSaved = mock(UserProfilePictureSaved.class);
        doReturn(Uni.createFrom().item(profilePictureSaved)).when(userProfilePictureRepository).save(userPseudo, "content".getBytes(), SupportedMediaType.IMAGE_JPEG);
        doReturn(Uni.createFrom().failure(RuntimeException::new)).when(userProfilePictureCacheRepository).evict(userPseudo);
        doReturn(Uni.createFrom().nullItem()).when(lockMechanism).lock(userPseudo);
        final Response response = mock(Response.class);
        doReturn(response).when(testResponseTransformer).toResponse(profilePictureSaved);

        // When
        final UniAssertSubscriber<Response> subscriber = saveUserProfilePictureUseCase.execute(
                new SaveUserProfilePictureCommand(userPseudo, "content".getBytes(), SupportedMediaType.IMAGE_JPEG),
                testResponseTransformer)
                .subscribe().withSubscriber(UniAssertSubscriber.create());

        // Then
        subscriber.assertCompleted().assertItem(response);
        verify(lockMechanism, times(1)).lock(any());
        verify(userProfilePictureRepository, times(1)).save(any(), any(), any());
        verify(userProfilePictureCacheRepository, times(1)).evict(any());
        verify(testResponseTransformer).toResponse(any(UserProfilePictureSaved.class));
        verify(lockMechanism, times(1)).unlock(any());
        verifyNoMoreInteractions(testResponseTransformer, userProfilePictureCacheRepository, userProfilePictureCacheRepository, lockMechanism);
    }

}
