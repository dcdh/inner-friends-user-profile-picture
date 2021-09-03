package com.innerfriends.userprofilepicture.domain.usecase;

import com.innerfriends.userprofilepicture.domain.*;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.helpers.test.UniAssertSubscriber;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

public class SaveUserProfilePictureUseCaseTest {

    public interface Response {

    }

    public interface TestResponseTransformer extends ResponseTransformer<Response> {

    }

    @Test
    public void should_save_user_profile_picture() {
        // Given
        final ProfilePictureRepository profilePictureRepository = mock(ProfilePictureRepository.class);
        final SaveUserProfilePictureUseCase<Response> saveUserProfilePictureUseCase = new SaveUserProfilePictureUseCase<>(profilePictureRepository);
        final TestResponseTransformer testResponseTransformer = mock(TestResponseTransformer.class);
        final UserPseudo userPseudo = mock(UserPseudo.class);
        final ProfilePictureSaved profilePictureSaved = mock(ProfilePictureSaved.class);
        doReturn(Uni.createFrom().item(profilePictureSaved)).when(profilePictureRepository).save(userPseudo, "content".getBytes(), SupportedMediaType.IMAGE_JPEG);
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
        verify(testResponseTransformer).toResponse(any(ProfilePictureSaved.class));
        verifyNoMoreInteractions(testResponseTransformer);
    }

    @Test
    public void should_handle_profile_picture_repository_exception() {
        // Given
        final ProfilePictureRepository profilePictureRepository = mock(ProfilePictureRepository.class);
        final SaveUserProfilePictureUseCase<Response> saveUserProfilePictureUseCase = new SaveUserProfilePictureUseCase<>(profilePictureRepository);
        final TestResponseTransformer testResponseTransformer = mock(TestResponseTransformer.class);
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
        verifyNoMoreInteractions(testResponseTransformer);
    }

    @Test
    public void should_handle_runtime_exception() {
        // Given
        final ProfilePictureRepository profilePictureRepository = mock(ProfilePictureRepository.class);
        final SaveUserProfilePictureUseCase<Response> saveUserProfilePictureUseCase = new SaveUserProfilePictureUseCase<>(profilePictureRepository);
        final TestResponseTransformer testResponseTransformer = mock(TestResponseTransformer.class);
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
        verifyNoMoreInteractions(testResponseTransformer);
    }

}
