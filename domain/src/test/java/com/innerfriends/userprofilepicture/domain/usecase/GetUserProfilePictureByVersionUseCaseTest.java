package com.innerfriends.userprofilepicture.domain.usecase;

import com.innerfriends.userprofilepicture.domain.*;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.helpers.test.UniAssertSubscriber;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class GetUserProfilePictureByVersionUseCaseTest {

    private UserProfilePictureRepository userProfilePictureRepository;
    private GetUserProfilePictureByVersionUseCase<Response> getUserProfilePictureByVersionUseCase;
    private TestResponseTransformer testResponseTransformer;

    @BeforeEach
    public void setup() {
        userProfilePictureRepository = mock(UserProfilePictureRepository.class);
        getUserProfilePictureByVersionUseCase = new GetUserProfilePictureByVersionUseCase<>(userProfilePictureRepository);
        testResponseTransformer = mock(TestResponseTransformer.class);
    }

    @Test
    public void should_get_featured_user_profile_picture() {
        // Given
        final UserPseudo userPseudo = mock(UserPseudo.class);
        final SupportedMediaType supportedMediaType = mock(SupportedMediaType.class);
        final VersionId versionId = mock(VersionId.class);
        final GetUserUserProfilePictureByVersionCommand getUserProfilePictureByVersionCommand = new GetUserUserProfilePictureByVersionCommand(userPseudo, supportedMediaType, versionId);
        final ContentUserProfilePicture contentProfilePicture = mock(ContentUserProfilePicture.class);

        doReturn(Uni.createFrom().item(contentProfilePicture)).when(userProfilePictureRepository).getContentByVersionId(getUserProfilePictureByVersionCommand);
        final Response response = mock(Response.class);
        doReturn(response).when(testResponseTransformer).toResponse(contentProfilePicture);

        // When
        final UniAssertSubscriber<Response> subscriber = getUserProfilePictureByVersionUseCase.execute(
                getUserProfilePictureByVersionCommand,
                testResponseTransformer)
                .subscribe().withSubscriber(UniAssertSubscriber.create());

        // Then
        subscriber.assertCompleted().assertItem(response);
        verify(userProfilePictureRepository, times(1)).getContentByVersionId(any());
        verify(testResponseTransformer).toResponse(any(ContentUserProfilePicture.class));
        verifyNoMoreInteractions(testResponseTransformer);
    }

    @Test
    public void should_handle_profile_picture_version_unknown_exception() {
        // Given
        final ProfilePictureVersionUnknownException profilePictureVersionUnknownException = mock(ProfilePictureVersionUnknownException.class);
        doReturn(Uni.createFrom().failure(profilePictureVersionUnknownException)).when(userProfilePictureRepository).getContentByVersionId(any());
        final Response response = mock(Response.class);
        doReturn(response).when(testResponseTransformer).toResponse(profilePictureVersionUnknownException);

        // When
        final UniAssertSubscriber<Response> subscriber = getUserProfilePictureByVersionUseCase.execute(
                new GetUserUserProfilePictureByVersionCommand(mock(UserPseudo.class), mock(SupportedMediaType.class), mock(VersionId.class)),
                testResponseTransformer)
                .subscribe().withSubscriber(UniAssertSubscriber.create());

        // Then
        subscriber.assertCompleted().assertItem(response);
        verify(userProfilePictureRepository, times(1)).getContentByVersionId(any());
        verify(testResponseTransformer).toResponse(any(ProfilePictureVersionUnknownException.class));
        verifyNoMoreInteractions(testResponseTransformer);
    }

    @Test
    public void should_handle_profile_picture_repository_exception() {
        // Given
        final ProfilePictureRepositoryException profilePictureRepositoryException = mock(ProfilePictureRepositoryException.class);
        doReturn(Uni.createFrom().failure(profilePictureRepositoryException)).when(userProfilePictureRepository).getContentByVersionId(any());
        final Response response = mock(Response.class);
        doReturn(response).when(testResponseTransformer).toResponse(profilePictureRepositoryException);

        // When
        final UniAssertSubscriber<Response> subscriber = getUserProfilePictureByVersionUseCase.execute(
                new GetUserUserProfilePictureByVersionCommand(mock(UserPseudo.class), mock(SupportedMediaType.class), mock(VersionId.class)),
                testResponseTransformer)
                .subscribe().withSubscriber(UniAssertSubscriber.create());

        // Then
        subscriber.assertCompleted().assertItem(response);
        verify(userProfilePictureRepository, times(1)).getContentByVersionId(any());
        verify(testResponseTransformer).toResponse(any(ProfilePictureRepositoryException.class));
        verifyNoMoreInteractions(testResponseTransformer);
    }

    @Test
    public void should_handle_runtime_exception() {
        // Given
        final RuntimeException runtimeException = new RuntimeException();
        doReturn(Uni.createFrom().failure(runtimeException)).when(userProfilePictureRepository).getContentByVersionId(any());
        final Response response = mock(Response.class);
        doReturn(response).when(testResponseTransformer).toResponse(runtimeException);

        // When
        final UniAssertSubscriber<Response> subscriber = getUserProfilePictureByVersionUseCase.execute(
                new GetUserUserProfilePictureByVersionCommand(mock(UserPseudo.class), mock(SupportedMediaType.class), mock(VersionId.class)),
                testResponseTransformer)
                .subscribe().withSubscriber(UniAssertSubscriber.create());

        // Then
        subscriber.assertCompleted().assertItem(response);
        verify(userProfilePictureRepository, times(1)).getContentByVersionId(any());
        verify(testResponseTransformer).toResponse(any(RuntimeException.class));
        verifyNoMoreInteractions(testResponseTransformer);
    }

}
