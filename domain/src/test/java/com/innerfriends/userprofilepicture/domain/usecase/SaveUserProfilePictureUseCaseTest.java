package com.innerfriends.userprofilepicture.domain.usecase;

import com.innerfriends.userprofilepicture.domain.*;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.helpers.test.UniAssertSubscriber;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import static org.mockito.Mockito.*;

public class SaveUserProfilePictureUseCaseTest {

    private ProfilePictureRepository profilePictureRepository;
    private SaveUserProfilePictureUseCase<Response> saveUserProfilePictureUseCase;
    private TestResponseTransformer testResponseTransformer;
    private UserProfilePictureCacheRepository userProfilePictureCacheRepository;

    @BeforeEach
    public void setup() {
        profilePictureRepository = mock(ProfilePictureRepository.class);
        userProfilePictureCacheRepository = mock(UserProfilePictureCacheRepository.class);
        saveUserProfilePictureUseCase = new SaveUserProfilePictureUseCase<>(profilePictureRepository, userProfilePictureCacheRepository);
        testResponseTransformer = mock(TestResponseTransformer.class);
    }

    @Test
    public void should_save_user_profile_picture_and_cache_it() {
        // Given
        final UserPseudo userPseudo = mock(UserPseudo.class);
        final ProfilePictureSaved profilePictureSaved = mock(ProfilePictureSaved.class);
        doReturn(Uni.createFrom().item(profilePictureSaved)).when(profilePictureRepository).save(userPseudo, "content".getBytes(), SupportedMediaType.IMAGE_JPEG);
        doReturn(Uni.createFrom().nullItem()).when(userProfilePictureCacheRepository).storeFeatured(userPseudo, profilePictureSaved);
        final Response response = mock(Response.class);
        doReturn(response).when(testResponseTransformer).toResponse(profilePictureSaved);
        final InOrder inOrder = inOrder(testResponseTransformer, profilePictureRepository, userProfilePictureCacheRepository);

        // When
        final UniAssertSubscriber<Response> subscriber = saveUserProfilePictureUseCase.execute(
                new SaveUserProfilePictureCommand(userPseudo, "content".getBytes(), SupportedMediaType.IMAGE_JPEG),
                testResponseTransformer)
                .subscribe().withSubscriber(UniAssertSubscriber.create());

        // Then
        subscriber.assertCompleted().assertItem(response);
        inOrder.verify(profilePictureRepository, times(1)).save(any(), any(), any());
        inOrder.verify(userProfilePictureCacheRepository, times(1)).storeFeatured(any(), any(ProfilePictureIdentifier.class));
        inOrder.verify(testResponseTransformer).toResponse(any(ProfilePictureSaved.class));
        verifyNoMoreInteractions(testResponseTransformer, userProfilePictureCacheRepository, userProfilePictureCacheRepository);
    }

    @Test
    public void should_handle_profile_picture_repository_exception() {
        // Given
        final ProfilePictureRepositoryException profilePictureRepositoryException = mock(ProfilePictureRepositoryException.class);
        doReturn(Uni.createFrom().failure(profilePictureRepositoryException)).when(profilePictureRepository).save(any(), any(), any());
        final Response response = mock(Response.class);
        doReturn(response).when(testResponseTransformer).toResponse(profilePictureRepositoryException);

        // When
        final UniAssertSubscriber<Response> subscriber = saveUserProfilePictureUseCase.execute(
                new SaveUserProfilePictureCommand(mock(UserPseudo.class), "content".getBytes(), SupportedMediaType.IMAGE_JPEG),
                testResponseTransformer)
                .subscribe().withSubscriber(UniAssertSubscriber.create());

        // Then
        subscriber.assertCompleted().assertItem(response);
        verify(profilePictureRepository, times(1)).save(any(), any(), any());
        verify(testResponseTransformer).toResponse(any(ProfilePictureRepositoryException.class));
        verifyNoMoreInteractions(testResponseTransformer, userProfilePictureCacheRepository, userProfilePictureCacheRepository);
    }

    @Test
    public void should_handle_runtime_exception() {
        // Given
        final RuntimeException runtimeException = new RuntimeException();
        doReturn(Uni.createFrom().failure(runtimeException)).when(profilePictureRepository).save(any(), any(), any());
        final Response response = mock(Response.class);
        doReturn(response).when(testResponseTransformer).toResponse(runtimeException);

        // When
        final UniAssertSubscriber<Response> subscriber = saveUserProfilePictureUseCase.execute(
                new SaveUserProfilePictureCommand(mock(UserPseudo.class), "content".getBytes(), SupportedMediaType.IMAGE_JPEG),
                testResponseTransformer)
                .subscribe().withSubscriber(UniAssertSubscriber.create());

        // Then
        subscriber.assertCompleted().assertItem(response);
        verify(profilePictureRepository, times(1)).save(any(), any(), any());
        verify(testResponseTransformer).toResponse(any(RuntimeException.class));
        verifyNoMoreInteractions(testResponseTransformer, userProfilePictureCacheRepository, userProfilePictureCacheRepository);
    }

    @Test
    public void should_save_user_profile_picture_when_cache_id_down() {
        // Given
        final UserPseudo userPseudo = mock(UserPseudo.class);
        final ProfilePictureSaved profilePictureSaved = mock(ProfilePictureSaved.class);
        doReturn(Uni.createFrom().item(profilePictureSaved)).when(profilePictureRepository).save(userPseudo, "content".getBytes(), SupportedMediaType.IMAGE_JPEG);
        doReturn(Uni.createFrom().failure(RuntimeException::new)).when(userProfilePictureCacheRepository).storeFeatured(userPseudo, profilePictureSaved);
        final Response response = mock(Response.class);
        doReturn(response).when(testResponseTransformer).toResponse(profilePictureSaved);

        // When
        final UniAssertSubscriber<Response> subscriber = saveUserProfilePictureUseCase.execute(
                new SaveUserProfilePictureCommand(userPseudo, "content".getBytes(), SupportedMediaType.IMAGE_JPEG),
                testResponseTransformer)
                .subscribe().withSubscriber(UniAssertSubscriber.create());

        // Then
        subscriber.assertCompleted().assertItem(response);
        verify(profilePictureRepository, times(1)).save(any(), any(), any());
        verify(userProfilePictureCacheRepository, times(1)).storeFeatured(any(), any(ProfilePictureIdentifier.class));
        verify(testResponseTransformer).toResponse(any(ProfilePictureSaved.class));
        verifyNoMoreInteractions(testResponseTransformer, userProfilePictureCacheRepository, userProfilePictureCacheRepository);
    }

}
