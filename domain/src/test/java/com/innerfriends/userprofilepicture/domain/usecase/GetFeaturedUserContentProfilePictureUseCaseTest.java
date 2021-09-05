package com.innerfriends.userprofilepicture.domain.usecase;

import com.innerfriends.userprofilepicture.domain.*;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.helpers.test.UniAssertSubscriber;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class GetFeaturedUserContentProfilePictureUseCaseTest {

    private ProfilePictureRepository profilePictureRepository;
    private GetFeaturedUserProfilePictureUseCase<Response> getFeaturedUserProfilePictureUseCase;
    private TestResponseTransformer testResponseTransformer;

    @BeforeEach
    public void setup() {
        profilePictureRepository = mock(ProfilePictureRepository.class);
        getFeaturedUserProfilePictureUseCase = new GetFeaturedUserProfilePictureUseCase<>(profilePictureRepository);
        testResponseTransformer = mock(TestResponseTransformer.class);
    }

    @Test
    public void should_get_featured_user_profile_picture() {
        // Given
        final UserPseudo userPseudo = mock(UserPseudo.class);
        final SupportedMediaType supportedMediaType = mock(SupportedMediaType.class);
        final ProfilePictureIdentifier profilePictureIdentifier = mock(ProfilePictureIdentifier.class);
        doReturn(Uni.createFrom().item(profilePictureIdentifier)).when(profilePictureRepository).getLast(userPseudo, supportedMediaType);
        final Response response = mock(Response.class);
        doReturn(response).when(testResponseTransformer).toResponse(profilePictureIdentifier);

        // When
        final UniAssertSubscriber<Response> subscriber = getFeaturedUserProfilePictureUseCase.execute(
                new GetFeaturedUserProfilePictureCommand(userPseudo, supportedMediaType),
                testResponseTransformer)
                .subscribe().withSubscriber(UniAssertSubscriber.create());

        // Then
        subscriber.assertCompleted().assertItem(response);
        verify(profilePictureRepository, times(1)).getLast(any(), any());
        verify(testResponseTransformer).toResponse(any(ProfilePictureIdentifier.class));
        verifyNoMoreInteractions(testResponseTransformer);
    }

    @Test
    public void should_handle_profile_picture_not_available_yet_exception() {
        // Given
        final ProfilePictureNotAvailableYetException profilePictureNotAvailableYetException = mock(ProfilePictureNotAvailableYetException.class);
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
        verify(profilePictureRepository, times(1)).getLast(any(), any());
        verify(testResponseTransformer).toResponse(any(ProfilePictureNotAvailableYetException.class));
        verifyNoMoreInteractions(testResponseTransformer);
    }

    @Test
    public void should_handle_profile_picture_repository_exception() {
        // Given
        final ProfilePictureRepositoryException profilePictureRepositoryException = mock(ProfilePictureRepositoryException.class);
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
        verify(profilePictureRepository, times(1)).getLast(any(), any());
        verify(testResponseTransformer).toResponse(any(ProfilePictureRepositoryException.class));
        verifyNoMoreInteractions(testResponseTransformer);
    }

    @Test
    public void should_handle_runtime_exception() {
        // Given
        final RuntimeException runtimeException = new RuntimeException();
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
        verify(profilePictureRepository, times(1)).getLast(any(), any());
        verify(testResponseTransformer).toResponse(any(RuntimeException.class));
        verifyNoMoreInteractions(testResponseTransformer);
    }
}
