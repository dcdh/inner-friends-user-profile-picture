package com.innerfriends.userprofilepicture.domain.usecase;

import com.innerfriends.userprofilepicture.domain.*;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.helpers.test.UniAssertSubscriber;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

public class SaveUserProfilePictureUseCaseTest {

    public interface Response {

    }

    public interface ResponseSaveUserProfilePictureResponseTransformer extends SaveUserProfilePictureResponseTransformer<Response> {

    }

    @Test
    public void should_save_user_profile_picture() {
        // Given
        final ProfilePictureRepository profilePictureRepository = mock(ProfilePictureRepository.class);
        final SaveUserProfilePictureUseCase<Response> saveUserProfilePictureUseCase = new SaveUserProfilePictureUseCase<>(profilePictureRepository);
        final ResponseSaveUserProfilePictureResponseTransformer responseSaveUserProfilePictureResponseTransformer = mock(ResponseSaveUserProfilePictureResponseTransformer.class);
        final UserPseudo userPseudo = mock(UserPseudo.class);
        final ProfilePictureSaved profilePictureSaved = mock(ProfilePictureSaved.class);
        doReturn(Uni.createFrom().item(profilePictureSaved)).when(profilePictureRepository).save(userPseudo, "content".getBytes(), SupportedMediaType.IMAGE_JPEG);
        final Response response = mock(Response.class);
        doReturn(response).when(responseSaveUserProfilePictureResponseTransformer).toResponse(profilePictureSaved);

        // When
        final UniAssertSubscriber<Response> subscriber = saveUserProfilePictureUseCase.execute(
                new SaveUserProfilePictureCommand(userPseudo, "content".getBytes(), SupportedMediaType.IMAGE_JPEG),
                responseSaveUserProfilePictureResponseTransformer)
                .subscribe().withSubscriber(UniAssertSubscriber.create());

        // Then
        subscriber.assertCompleted().assertItem(response);
        verify(profilePictureRepository, times(1)).save(any(), any(), any());
        verify(responseSaveUserProfilePictureResponseTransformer).toResponse(any(ProfilePictureSaved.class));
        verifyNoMoreInteractions(responseSaveUserProfilePictureResponseTransformer);
    }

    @Test
    public void should_handle_profile_picture_repository_exception() {
        // Given
        final ProfilePictureRepository profilePictureRepository = mock(ProfilePictureRepository.class);
        final SaveUserProfilePictureUseCase<Response> saveUserProfilePictureUseCase = new SaveUserProfilePictureUseCase<>(profilePictureRepository);
        final ResponseSaveUserProfilePictureResponseTransformer responseSaveUserProfilePictureResponseTransformer = mock(ResponseSaveUserProfilePictureResponseTransformer.class);
        final ProfilePictureRepositoryException profilePictureRepositoryException = mock(ProfilePictureRepositoryException.class);
        doReturn(Uni.createFrom().failure(profilePictureRepositoryException)).when(profilePictureRepository).save(any(), any(), any());
        final Response response = mock(Response.class);
        doReturn(response).when(responseSaveUserProfilePictureResponseTransformer).toResponse(profilePictureRepositoryException);

        // When
        final UniAssertSubscriber<Response> subscriber = saveUserProfilePictureUseCase.execute(
                new SaveUserProfilePictureCommand(mock(UserPseudo.class), "content".getBytes(), SupportedMediaType.IMAGE_JPEG),
                responseSaveUserProfilePictureResponseTransformer)
                .subscribe().withSubscriber(UniAssertSubscriber.create());

        // Then
        subscriber.assertCompleted().assertItem(response);
        verify(profilePictureRepository, times(1)).save(any(), any(), any());
        verify(responseSaveUserProfilePictureResponseTransformer).toResponse(any(ProfilePictureRepositoryException.class));
        verifyNoMoreInteractions(responseSaveUserProfilePictureResponseTransformer);
    }

    @Test
    public void should_handle_runtime_exception() {
        // Given
        final ProfilePictureRepository profilePictureRepository = mock(ProfilePictureRepository.class);
        final SaveUserProfilePictureUseCase<Response> saveUserProfilePictureUseCase = new SaveUserProfilePictureUseCase<>(profilePictureRepository);
        final ResponseSaveUserProfilePictureResponseTransformer responseSaveUserProfilePictureResponseTransformer = mock(ResponseSaveUserProfilePictureResponseTransformer.class);
        final RuntimeException runtimeException = new RuntimeException();
        doReturn(Uni.createFrom().failure(runtimeException)).when(profilePictureRepository).save(any(), any(), any());
        final Response response = mock(Response.class);
        doReturn(response).when(responseSaveUserProfilePictureResponseTransformer).toResponse(runtimeException);

        // When
        final UniAssertSubscriber<Response> subscriber = saveUserProfilePictureUseCase.execute(
                new SaveUserProfilePictureCommand(mock(UserPseudo.class), "content".getBytes(), SupportedMediaType.IMAGE_JPEG),
                responseSaveUserProfilePictureResponseTransformer)
                .subscribe().withSubscriber(UniAssertSubscriber.create());

        // Then
        subscriber.assertCompleted().assertItem(response);
        verify(profilePictureRepository, times(1)).save(any(), any(), any());
        verify(responseSaveUserProfilePictureResponseTransformer).toResponse(any(RuntimeException.class));
        verifyNoMoreInteractions(responseSaveUserProfilePictureResponseTransformer);
    }

}
