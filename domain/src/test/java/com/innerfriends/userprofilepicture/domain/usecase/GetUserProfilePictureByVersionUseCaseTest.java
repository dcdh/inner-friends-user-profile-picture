package com.innerfriends.userprofilepicture.domain.usecase;

import com.innerfriends.userprofilepicture.domain.*;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.helpers.test.UniAssertSubscriber;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class GetUserProfilePictureByVersionUseCaseTest {

    private UserProfilePictureRepository userProfilePictureRepository;
    private GetUserProfilePictureByVersionUseCase<Response> getUserProfilePictureByVersionUseCase;
    private TestResponseTransformer testResponseTransformer;
    private LockMechanism lockMechanism;

    @BeforeEach
    public void setup() {
        userProfilePictureRepository = mock(UserProfilePictureRepository.class);
        lockMechanism = mock(LockMechanism.class);
        getUserProfilePictureByVersionUseCase = new GetUserProfilePictureByVersionUseCase<>(userProfilePictureRepository, lockMechanism);
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
        doReturn(Uni.createFrom().nullItem()).when(lockMechanism).lock(userPseudo);
        final InOrder inOrder = inOrder(testResponseTransformer, userProfilePictureRepository, lockMechanism);

        // When
        final UniAssertSubscriber<Response> subscriber = getUserProfilePictureByVersionUseCase.execute(
                getUserProfilePictureByVersionCommand,
                testResponseTransformer)
                .subscribe().withSubscriber(UniAssertSubscriber.create());

        // Then
        subscriber.assertCompleted().assertItem(response);
        inOrder.verify(lockMechanism, times(1)).lock(userPseudo);
        inOrder.verify(userProfilePictureRepository, times(1)).getContentByVersionId(any());
        inOrder.verify(testResponseTransformer).toResponse(any(ContentUserProfilePicture.class));
        inOrder.verify(lockMechanism, times(1)).unlock(userPseudo);
        verifyNoMoreInteractions(testResponseTransformer, lockMechanism);
    }

    @Test
    public void should_handle_profile_picture_version_unknown_exception() {
        // Given
        final UserProfilePictureVersionUnknownException userProfilePictureVersionUnknownException = mock(UserProfilePictureVersionUnknownException.class);
        doReturn(Uni.createFrom().failure(userProfilePictureVersionUnknownException)).when(userProfilePictureRepository).getContentByVersionId(any());
        doReturn(Uni.createFrom().nullItem()).when(lockMechanism).lock(any());
        final Response response = mock(Response.class);
        doReturn(response).when(testResponseTransformer).toResponse(userProfilePictureVersionUnknownException);

        // When
        final UniAssertSubscriber<Response> subscriber = getUserProfilePictureByVersionUseCase.execute(
                new GetUserUserProfilePictureByVersionCommand(mock(UserPseudo.class), mock(SupportedMediaType.class), mock(VersionId.class)),
                testResponseTransformer)
                .subscribe().withSubscriber(UniAssertSubscriber.create());

        // Then
        subscriber.assertCompleted().assertItem(response);
        verify(lockMechanism, times(1)).lock(any());
        verify(userProfilePictureRepository, times(1)).getContentByVersionId(any());
        verify(testResponseTransformer).toResponse(any(UserProfilePictureVersionUnknownException.class));
        verify(lockMechanism, times(1)).unlock(any());
        verifyNoMoreInteractions(testResponseTransformer, lockMechanism);
    }

    @Test
    public void should_handle_profile_picture_repository_exception() {
        // Given
        final UserProfilePictureRepositoryException userProfilePictureRepositoryException = mock(UserProfilePictureRepositoryException.class);
        doReturn(Uni.createFrom().failure(userProfilePictureRepositoryException)).when(userProfilePictureRepository).getContentByVersionId(any());
        doReturn(Uni.createFrom().nullItem()).when(lockMechanism).lock(any());
        final Response response = mock(Response.class);
        doReturn(response).when(testResponseTransformer).toResponse(userProfilePictureRepositoryException);

        // When
        final UniAssertSubscriber<Response> subscriber = getUserProfilePictureByVersionUseCase.execute(
                new GetUserUserProfilePictureByVersionCommand(mock(UserPseudo.class), mock(SupportedMediaType.class), mock(VersionId.class)),
                testResponseTransformer)
                .subscribe().withSubscriber(UniAssertSubscriber.create());

        // Then
        subscriber.assertCompleted().assertItem(response);
        verify(lockMechanism, times(1)).lock(any());
        verify(userProfilePictureRepository, times(1)).getContentByVersionId(any());
        verify(testResponseTransformer).toResponse(any(UserProfilePictureRepositoryException.class));
        verify(lockMechanism, times(1)).unlock(any());
        verifyNoMoreInteractions(testResponseTransformer, lockMechanism);
    }

    @Test
    public void should_handle_runtime_exception() {
        // Given
        final RuntimeException runtimeException = new RuntimeException();
        doReturn(Uni.createFrom().failure(runtimeException)).when(userProfilePictureRepository).getContentByVersionId(any());
        doReturn(Uni.createFrom().nullItem()).when(lockMechanism).lock(any());
        final Response response = mock(Response.class);
        doReturn(response).when(testResponseTransformer).toResponse(runtimeException);

        // When
        final UniAssertSubscriber<Response> subscriber = getUserProfilePictureByVersionUseCase.execute(
                new GetUserUserProfilePictureByVersionCommand(mock(UserPseudo.class), mock(SupportedMediaType.class), mock(VersionId.class)),
                testResponseTransformer)
                .subscribe().withSubscriber(UniAssertSubscriber.create());

        // Then
        subscriber.assertCompleted().assertItem(response);
        verify(lockMechanism, times(1)).lock(any());
        verify(userProfilePictureRepository, times(1)).getContentByVersionId(any());
        verify(testResponseTransformer).toResponse(any(RuntimeException.class));
        verify(lockMechanism, times(1)).unlock(any());
        verifyNoMoreInteractions(testResponseTransformer, lockMechanism);
    }

}
