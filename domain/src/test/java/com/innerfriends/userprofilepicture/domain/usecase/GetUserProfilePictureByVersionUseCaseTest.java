package com.innerfriends.userprofilepicture.domain.usecase;

import com.innerfriends.userprofilepicture.domain.*;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.helpers.test.UniAssertSubscriber;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class GetUserProfilePictureByVersionUseCaseTest {

    private ProfilePictureRepository profilePictureRepository;
    private GetUserProfilePictureByVersionUseCase<Response> getUserProfilePictureByVersionUseCase;
    private TestResponseTransformer testResponseTransformer;

    @BeforeEach
    public void setup() {
        profilePictureRepository = mock(ProfilePictureRepository.class);
        getUserProfilePictureByVersionUseCase = new GetUserProfilePictureByVersionUseCase<>(profilePictureRepository);
        testResponseTransformer = mock(TestResponseTransformer.class);
    }

    @Test
    public void should_get_featured_user_profile_picture() {
        // Given
        final UserPseudo userPseudo = mock(UserPseudo.class);
        final SupportedMediaType supportedMediaType = mock(SupportedMediaType.class);
        final VersionId versionId = mock(VersionId.class);
        final GetUserProfilePictureByVersionCommand getUserProfilePictureByVersionCommand = new GetUserProfilePictureByVersionCommand(userPseudo, supportedMediaType, versionId);
        final ContentProfilePicture contentProfilePicture = mock(ContentProfilePicture.class);

        doReturn(Uni.createFrom().item(contentProfilePicture)).when(profilePictureRepository).getContentByVersionId(getUserProfilePictureByVersionCommand);
        final Response response = mock(Response.class);
        doReturn(response).when(testResponseTransformer).toResponse(contentProfilePicture);

        // When
        final UniAssertSubscriber<Response> subscriber = getUserProfilePictureByVersionUseCase.execute(
                getUserProfilePictureByVersionCommand,
                testResponseTransformer)
                .subscribe().withSubscriber(UniAssertSubscriber.create());

        // Then
        subscriber.assertCompleted().assertItem(response);
        verify(profilePictureRepository, times(1)).getContentByVersionId(any());
        verify(testResponseTransformer).toResponse(any(ContentProfilePicture.class));
        verifyNoMoreInteractions(testResponseTransformer);
    }

    @Test
    public void should_handle_profile_picture_version_unknown_exception() {
        // Given
        final ProfilePictureVersionUnknownException profilePictureVersionUnknownException = mock(ProfilePictureVersionUnknownException.class);
        doReturn(Uni.createFrom().failure(profilePictureVersionUnknownException)).when(profilePictureRepository).getContentByVersionId(any());
        final Response response = mock(Response.class);
        doReturn(response).when(testResponseTransformer).toResponse(profilePictureVersionUnknownException);

        // When
        final UniAssertSubscriber<Response> subscriber = getUserProfilePictureByVersionUseCase.execute(
                new GetUserProfilePictureByVersionCommand(mock(UserPseudo.class), mock(SupportedMediaType.class), mock(VersionId.class)),
                testResponseTransformer)
                .subscribe().withSubscriber(UniAssertSubscriber.create());

        // Then
        subscriber.assertCompleted().assertItem(response);
        verify(profilePictureRepository, times(1)).getContentByVersionId(any());
        verify(testResponseTransformer).toResponse(any(ProfilePictureVersionUnknownException.class));
        verifyNoMoreInteractions(testResponseTransformer);
    }

    @Test
    public void should_handle_profile_picture_repository_exception() {
        // Given
        final ProfilePictureRepositoryException profilePictureRepositoryException = mock(ProfilePictureRepositoryException.class);
        doReturn(Uni.createFrom().failure(profilePictureRepositoryException)).when(profilePictureRepository).getContentByVersionId(any());
        final Response response = mock(Response.class);
        doReturn(response).when(testResponseTransformer).toResponse(profilePictureRepositoryException);

        // When
        final UniAssertSubscriber<Response> subscriber = getUserProfilePictureByVersionUseCase.execute(
                new GetUserProfilePictureByVersionCommand(mock(UserPseudo.class), mock(SupportedMediaType.class), mock(VersionId.class)),
                testResponseTransformer)
                .subscribe().withSubscriber(UniAssertSubscriber.create());

        // Then
        subscriber.assertCompleted().assertItem(response);
        verify(profilePictureRepository, times(1)).getContentByVersionId(any());
        verify(testResponseTransformer).toResponse(any(ProfilePictureRepositoryException.class));
        verifyNoMoreInteractions(testResponseTransformer);
    }

    @Test
    public void should_handle_runtime_exception() {
        // Given
        final RuntimeException runtimeException = new RuntimeException();
        doReturn(Uni.createFrom().failure(runtimeException)).when(profilePictureRepository).getContentByVersionId(any());
        final Response response = mock(Response.class);
        doReturn(response).when(testResponseTransformer).toResponse(runtimeException);

        // When
        final UniAssertSubscriber<Response> subscriber = getUserProfilePictureByVersionUseCase.execute(
                new GetUserProfilePictureByVersionCommand(mock(UserPseudo.class), mock(SupportedMediaType.class), mock(VersionId.class)),
                testResponseTransformer)
                .subscribe().withSubscriber(UniAssertSubscriber.create());

        // Then
        subscriber.assertCompleted().assertItem(response);
        verify(profilePictureRepository, times(1)).getContentByVersionId(any());
        verify(testResponseTransformer).toResponse(any(RuntimeException.class));
        verifyNoMoreInteractions(testResponseTransformer);
    }

}
