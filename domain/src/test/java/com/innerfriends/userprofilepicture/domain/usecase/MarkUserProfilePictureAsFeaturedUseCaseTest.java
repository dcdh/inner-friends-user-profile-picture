package com.innerfriends.userprofilepicture.domain.usecase;

import com.innerfriends.userprofilepicture.domain.*;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.helpers.test.UniAssertSubscriber;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verifyNoMoreInteractions;

public class MarkUserProfilePictureAsFeaturedUseCaseTest {

    private UserProfilPictureFeaturedRepository userProfilPictureFeaturedRepository;
    private UserProfilePictureCacheRepository userProfilePictureCacheRepository;
    private LockMechanism lockMechanism;
    private MarkUserProfilePictureAsFeaturedUseCase<Response> markUserProfilePictureAsFeaturedUseCase;
    private TestResponseTransformer testResponseTransformer;

    @BeforeEach
    public void setup() {
        userProfilPictureFeaturedRepository = mock(UserProfilPictureFeaturedRepository.class);
        userProfilePictureCacheRepository = mock(UserProfilePictureCacheRepository.class);
        lockMechanism = mock(LockMechanism.class);
        markUserProfilePictureAsFeaturedUseCase = new MarkUserProfilePictureAsFeaturedUseCase<>(
                userProfilPictureFeaturedRepository, userProfilePictureCacheRepository, lockMechanism
        );
        testResponseTransformer = mock(TestResponseTransformer.class);
    }

    @Test
    public void should_mark_user_profile_picture_as_featured_and_evict_it() {
        // Given
        final UserPseudo userPseudo = mock(UserPseudo.class);
        final SupportedMediaType supportedMediaType = mock(SupportedMediaType.class);
        final VersionId versionId = mock(VersionId.class);
        final UserProfilePictureIdentifier userProfilePictureIdentifier = mock(UserProfilePictureIdentifier.class);
        doReturn(Uni.createFrom().nullItem()).when(lockMechanism).lock(userPseudo);
        doReturn(Uni.createFrom().item(userProfilePictureIdentifier)).when(userProfilPictureFeaturedRepository).markAsFeatured(new MarkUserProfilePictureAsFeaturedCommand(userPseudo, supportedMediaType, versionId));
        doReturn(Uni.createFrom().nullItem()).when(userProfilePictureCacheRepository).evict(userPseudo);
        final Response response = mock(Response.class);
        doReturn(response).when(testResponseTransformer).toResponse(userProfilePictureIdentifier);
        final InOrder inOrder = inOrder(testResponseTransformer, userProfilPictureFeaturedRepository, userProfilePictureCacheRepository, lockMechanism);

        // When
        final UniAssertSubscriber<Response> subscriber = markUserProfilePictureAsFeaturedUseCase.execute(
                new MarkUserProfilePictureAsFeaturedCommand(userPseudo, supportedMediaType, versionId),
                testResponseTransformer).subscribe().withSubscriber(UniAssertSubscriber.create());

        // Then
        subscriber.assertCompleted().assertItem(response);
        inOrder.verify(lockMechanism, times(1)).lock(userPseudo);
        inOrder.verify(userProfilPictureFeaturedRepository, times(1)).markAsFeatured(any());
        inOrder.verify(userProfilePictureCacheRepository, times(1)).evict(any());
        inOrder.verify(testResponseTransformer).toResponse(any(UserProfilePictureIdentifier.class));
        inOrder.verify(lockMechanism, times(1)).unlock(userPseudo);
        verifyNoMoreInteractions(testResponseTransformer, userProfilePictureCacheRepository, userProfilPictureFeaturedRepository, lockMechanism);
    }

    @Test
    public void should_handle_user_profile_picture_featured_repository_exception() {
        // Given
        final UserPseudo userPseudo = mock(UserPseudo.class);
        final SupportedMediaType supportedMediaType = mock(SupportedMediaType.class);
        final VersionId versionId = mock(VersionId.class);
        doReturn(Uni.createFrom().nullItem()).when(lockMechanism).lock(userPseudo);
        final UserProfilPictureFeaturedRepositoryException userProfilPictureFeaturedRepositoryException = new UserProfilPictureFeaturedRepositoryException();
        doReturn(Uni.createFrom().failure(userProfilPictureFeaturedRepositoryException)).when(userProfilPictureFeaturedRepository)
                .markAsFeatured(new MarkUserProfilePictureAsFeaturedCommand(userPseudo, supportedMediaType, versionId));
        final Response response = mock(Response.class);
        doReturn(response).when(testResponseTransformer).toResponse(userProfilPictureFeaturedRepositoryException);
        final InOrder inOrder = inOrder(testResponseTransformer, userProfilPictureFeaturedRepository, userProfilePictureCacheRepository, lockMechanism);

        // When
        final UniAssertSubscriber<Response> subscriber = markUserProfilePictureAsFeaturedUseCase.execute(
                new MarkUserProfilePictureAsFeaturedCommand(userPseudo, supportedMediaType, versionId),
                testResponseTransformer).subscribe().withSubscriber(UniAssertSubscriber.create());

        // Then
        subscriber.assertCompleted().assertItem(response);
        inOrder.verify(lockMechanism, times(1)).lock(userPseudo);
        inOrder.verify(userProfilPictureFeaturedRepository, times(1)).markAsFeatured(any());
        inOrder.verify(testResponseTransformer).toResponse(any(UserProfilPictureFeaturedRepositoryException.class));
        inOrder.verify(lockMechanism, times(1)).unlock(userPseudo);
        verifyNoMoreInteractions(testResponseTransformer, userProfilePictureCacheRepository, userProfilPictureFeaturedRepository, lockMechanism);
    }

    @Test
    public void should_handle_runtime_exception() {
        // Given
        final UserPseudo userPseudo = mock(UserPseudo.class);
        final SupportedMediaType supportedMediaType = mock(SupportedMediaType.class);
        final VersionId versionId = mock(VersionId.class);
        doReturn(Uni.createFrom().nullItem()).when(lockMechanism).lock(userPseudo);
        final RuntimeException runtimeException = new RuntimeException();
        doReturn(Uni.createFrom().failure(runtimeException)).when(userProfilPictureFeaturedRepository)
                .markAsFeatured(new MarkUserProfilePictureAsFeaturedCommand(userPseudo, supportedMediaType, versionId));
        final Response response = mock(Response.class);
        doReturn(response).when(testResponseTransformer).toResponse(runtimeException);
        final InOrder inOrder = inOrder(testResponseTransformer, userProfilPictureFeaturedRepository, userProfilePictureCacheRepository, lockMechanism);

        // When
        final UniAssertSubscriber<Response> subscriber = markUserProfilePictureAsFeaturedUseCase.execute(
                new MarkUserProfilePictureAsFeaturedCommand(userPseudo, supportedMediaType, versionId),
                testResponseTransformer).subscribe().withSubscriber(UniAssertSubscriber.create());

        // Then
        subscriber.assertCompleted().assertItem(response);
        inOrder.verify(lockMechanism, times(1)).lock(userPseudo);
        inOrder.verify(userProfilPictureFeaturedRepository, times(1)).markAsFeatured(any());
        inOrder.verify(testResponseTransformer).toResponse(any(RuntimeException.class));
        inOrder.verify(lockMechanism, times(1)).unlock(userPseudo);
        verifyNoMoreInteractions(testResponseTransformer, userProfilePictureCacheRepository, userProfilPictureFeaturedRepository, lockMechanism);
    }

    @Test
    public void should_mark_user_profile_picture_as_featured_when_cache_id_down() {
        // Given
        final UserPseudo userPseudo = mock(UserPseudo.class);
        final SupportedMediaType supportedMediaType = mock(SupportedMediaType.class);
        final VersionId versionId = mock(VersionId.class);
        final UserProfilePictureIdentifier userProfilePictureIdentifier = mock(UserProfilePictureIdentifier.class);
        doReturn(Uni.createFrom().nullItem()).when(lockMechanism).lock(userPseudo);
        doReturn(Uni.createFrom().item(userProfilePictureIdentifier)).when(userProfilPictureFeaturedRepository).markAsFeatured(new MarkUserProfilePictureAsFeaturedCommand(userPseudo, supportedMediaType, versionId));
        doReturn(Uni.createFrom().failure(RuntimeException::new)).when(userProfilePictureCacheRepository).evict(userPseudo);
        final Response response = mock(Response.class);
        doReturn(response).when(testResponseTransformer).toResponse(userProfilePictureIdentifier);
        final InOrder inOrder = inOrder(testResponseTransformer, userProfilPictureFeaturedRepository, userProfilePictureCacheRepository, lockMechanism);

        // When
        final UniAssertSubscriber<Response> subscriber = markUserProfilePictureAsFeaturedUseCase.execute(
                new MarkUserProfilePictureAsFeaturedCommand(userPseudo, supportedMediaType, versionId),
                testResponseTransformer).subscribe().withSubscriber(UniAssertSubscriber.create());

        // Then
        subscriber.assertCompleted().assertItem(response);
        inOrder.verify(lockMechanism, times(1)).lock(userPseudo);
        inOrder.verify(userProfilPictureFeaturedRepository, times(1)).markAsFeatured(any());
        inOrder.verify(userProfilePictureCacheRepository, times(1)).evict(any());
        inOrder.verify(testResponseTransformer).toResponse(any(UserProfilePictureIdentifier.class));
        inOrder.verify(lockMechanism, times(1)).unlock(userPseudo);
        verifyNoMoreInteractions(testResponseTransformer, userProfilePictureCacheRepository, userProfilPictureFeaturedRepository, lockMechanism);
    }

}
