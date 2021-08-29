package com.innerfriends.userprofilepicture.domain.usecase;

import com.innerfriends.userprofilepicture.domain.*;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.helpers.test.UniAssertSubscriber;
import org.junit.jupiter.api.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class GetLastUserProfilePictureUseCaseTest {

    public interface Response {

    }

    public interface ResponseGetLastUserProfilePictureResponseTransformer extends GetLastUserProfilePictureResponseTransformer<Response> {

    }

    @Test
    public void should_get_last_user_profile_picture() {
        // Given
        final ProfilePictureRepository profilePictureRepository = mock(ProfilePictureRepository.class);
        final GetLastUserProfilePictureUseCase<Response> getLastUserProfilePictureUseCase = new GetLastUserProfilePictureUseCase<>(profilePictureRepository);
        final ResponseGetLastUserProfilePictureResponseTransformer responseGetLastUserProfilePictureResponseTransformer = mock(ResponseGetLastUserProfilePictureResponseTransformer.class);
        final UserPseudo userPseudo = mock(UserPseudo.class);
        final ProfilePicture profilePicture = mock(ProfilePicture.class);
        doReturn(Uni.createFrom().item(profilePicture)).when(profilePictureRepository).getLast(userPseudo);
        final Response response = mock(Response.class);
        doReturn(response).when(responseGetLastUserProfilePictureResponseTransformer).toResponse(profilePicture);

        // When
        final UniAssertSubscriber<Response> subscriber = getLastUserProfilePictureUseCase.execute(
                new GetLastUserProfilePictureCommand(userPseudo),
                responseGetLastUserProfilePictureResponseTransformer)
                .subscribe().withSubscriber(UniAssertSubscriber.create());

        // Then
        subscriber.assertCompleted().assertItem(response);
        verify(profilePictureRepository, times(1)).getLast(any());
        verify(responseGetLastUserProfilePictureResponseTransformer).toResponse(any(ProfilePicture.class));
        verifyNoMoreInteractions(responseGetLastUserProfilePictureResponseTransformer);
    }

    @Test
    public void should_handle_profile_picture_not_available_yet_exception() {
        // Given
        final ProfilePictureRepository profilePictureRepository = mock(ProfilePictureRepository.class);
        final GetLastUserProfilePictureUseCase<Response> getLastUserProfilePictureUseCase = new GetLastUserProfilePictureUseCase<>(profilePictureRepository);
        final ResponseGetLastUserProfilePictureResponseTransformer responseGetLastUserProfilePictureResponseTransformer = mock(ResponseGetLastUserProfilePictureResponseTransformer.class);
        final ProfilePictureNotAvailableYetException profilePictureNotAvailableYetException = mock(ProfilePictureNotAvailableYetException.class);
        doReturn(Uni.createFrom().failure(profilePictureNotAvailableYetException)).when(profilePictureRepository).getLast(any());
        final Response response = mock(Response.class);
        doReturn(response).when(responseGetLastUserProfilePictureResponseTransformer).toResponse(profilePictureNotAvailableYetException);

        // When
        final UniAssertSubscriber<Response> subscriber = getLastUserProfilePictureUseCase.execute(
                new GetLastUserProfilePictureCommand(mock(UserPseudo.class)),
                responseGetLastUserProfilePictureResponseTransformer)
                .subscribe().withSubscriber(UniAssertSubscriber.create());

        // Then
        subscriber.assertCompleted().assertItem(response);
        verify(profilePictureRepository, times(1)).getLast(any());
        verify(responseGetLastUserProfilePictureResponseTransformer).toResponse(any(ProfilePictureNotAvailableYetException.class));
        verifyNoMoreInteractions(responseGetLastUserProfilePictureResponseTransformer);
    }

    @Test
    public void should_handle_profile_picture_repository_exception() {
        // Given
        final ProfilePictureRepository profilePictureRepository = mock(ProfilePictureRepository.class);
        final GetLastUserProfilePictureUseCase<Response> getLastUserProfilePictureUseCase = new GetLastUserProfilePictureUseCase<>(profilePictureRepository);
        final ResponseGetLastUserProfilePictureResponseTransformer responseGetLastUserProfilePictureResponseTransformer = mock(ResponseGetLastUserProfilePictureResponseTransformer.class);
        final ProfilePictureRepositoryException profilePictureRepositoryException = mock(ProfilePictureRepositoryException.class);
        doReturn(Uni.createFrom().failure(profilePictureRepositoryException)).when(profilePictureRepository).getLast(any());
        final Response response = mock(Response.class);
        doReturn(response).when(responseGetLastUserProfilePictureResponseTransformer).toResponse(profilePictureRepositoryException);

        // When
        final UniAssertSubscriber<Response> subscriber = getLastUserProfilePictureUseCase.execute(
                new GetLastUserProfilePictureCommand(mock(UserPseudo.class)),
                responseGetLastUserProfilePictureResponseTransformer)
                .subscribe().withSubscriber(UniAssertSubscriber.create());

        // Then
        subscriber.assertCompleted().assertItem(response);
        verify(profilePictureRepository, times(1)).getLast(any());
        verify(responseGetLastUserProfilePictureResponseTransformer).toResponse(any(ProfilePictureRepositoryException.class));
        verifyNoMoreInteractions(responseGetLastUserProfilePictureResponseTransformer);
    }

    @Test
    public void should_handle_runtime_exception() {
        // Given
        final ProfilePictureRepository profilePictureRepository = mock(ProfilePictureRepository.class);
        final GetLastUserProfilePictureUseCase<Response> getLastUserProfilePictureUseCase = new GetLastUserProfilePictureUseCase<>(profilePictureRepository);
        final ResponseGetLastUserProfilePictureResponseTransformer responseGetLastUserProfilePictureResponseTransformer = mock(ResponseGetLastUserProfilePictureResponseTransformer.class);
        final RuntimeException runtimeException = new RuntimeException();
        doReturn(Uni.createFrom().failure(runtimeException)).when(profilePictureRepository).getLast(any());
        final Response response = mock(Response.class);
        doReturn(response).when(responseGetLastUserProfilePictureResponseTransformer).toResponse(runtimeException);

        // When
        final UniAssertSubscriber<Response> subscriber = getLastUserProfilePictureUseCase.execute(
                new GetLastUserProfilePictureCommand(mock(UserPseudo.class)),
                responseGetLastUserProfilePictureResponseTransformer)
                .subscribe().withSubscriber(UniAssertSubscriber.create());

        // Then
        subscriber.assertCompleted().assertItem(response);
        verify(profilePictureRepository, times(1)).getLast(any());
        verify(responseGetLastUserProfilePictureResponseTransformer).toResponse(any(RuntimeException.class));
        verifyNoMoreInteractions(responseGetLastUserProfilePictureResponseTransformer);
    }
}
