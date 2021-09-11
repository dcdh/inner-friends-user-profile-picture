package com.innerfriends.userprofilepicture.domain.usecase;

import com.innerfriends.userprofilepicture.domain.*;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.helpers.test.UniAssertSubscriber;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class ListUserProfilPicturesUseCaseTest {

    private ProfilePictureRepository profilePictureRepository;
    private ListUserProfilPicturesUseCase<Response> listUserProfilPicturesUseCase;
    private TestResponseTransformer testResponseTransformer;
    private UserProfilePictureCacheRepository userProfilePictureCacheRepository;

    @BeforeEach
    public void setup() {
        profilePictureRepository = mock(ProfilePictureRepository.class);
        userProfilePictureCacheRepository = mock(UserProfilePictureCacheRepository.class);
        listUserProfilPicturesUseCase = new ListUserProfilPicturesUseCase<>(profilePictureRepository, userProfilePictureCacheRepository);
        testResponseTransformer = mock(TestResponseTransformer.class);
    }

    @Test
    public void should_get_featured_user_profile_picture_from_cache_when_present() {
        // Given
        final UserPseudo userPseudo = mock(UserPseudo.class);
        final SupportedMediaType supportedMediaType = mock(SupportedMediaType.class);
        final CachedUserProfilePictures cachedUserProfilePictures = mock(CachedUserProfilePictures.class);
        final List<ProfilePictureIdentifier> profilePictureIdentifiers = Collections.emptyList();
        doReturn(profilePictureIdentifiers).when(cachedUserProfilePictures).profilePictureIdentifiers();
        doReturn(Uni.createFrom().item(cachedUserProfilePictures)).when(userProfilePictureCacheRepository).get(userPseudo);
        final Response response = mock(Response.class);
        doReturn(response).when(testResponseTransformer).toResponse(profilePictureIdentifiers);

        // When
        final UniAssertSubscriber<Response> subscriber = listUserProfilPicturesUseCase.execute(
                new ListUserProfilPicturesCommand(userPseudo, supportedMediaType),
                testResponseTransformer)
                .subscribe().withSubscriber(UniAssertSubscriber.create());

        // Then
        subscriber.assertCompleted().assertItem(response);
        verify(userProfilePictureCacheRepository, times(1)).get(any());
        verify(cachedUserProfilePictures, times(1)).profilePictureIdentifiers();
        verify(testResponseTransformer).toResponse(any(List.class));
        verifyNoMoreInteractions(testResponseTransformer, userProfilePictureCacheRepository, userProfilePictureCacheRepository);
    }

    @Test
    public void should_get_featured_user_profile_picture_and_cache_it_when_not_in_cache() {
        // Given
        final UserPseudo userPseudo = mock(UserPseudo.class);
        final SupportedMediaType supportedMediaType = mock(SupportedMediaType.class);
        final List<ProfilePictureIdentifier> profilePictureIdentifiers = Collections.emptyList();
        doReturn(Uni.createFrom().nullItem()).when(userProfilePictureCacheRepository).get(userPseudo);
        doReturn(Uni.createFrom().item(profilePictureIdentifiers)).when(profilePictureRepository).listByUserPseudo(userPseudo, supportedMediaType);
        doReturn(Uni.createFrom().nullItem()).when(userProfilePictureCacheRepository).store(userPseudo, profilePictureIdentifiers);

        final Response response = mock(Response.class);
        doReturn(response).when(testResponseTransformer).toResponse(profilePictureIdentifiers);
        final InOrder inOrder = inOrder(testResponseTransformer, profilePictureRepository, userProfilePictureCacheRepository);

        // When
        final UniAssertSubscriber<Response> subscriber = listUserProfilPicturesUseCase.execute(
                new ListUserProfilPicturesCommand(userPseudo, supportedMediaType),
                testResponseTransformer)
                .subscribe().withSubscriber(UniAssertSubscriber.create());

        // Then
        subscriber.assertCompleted().assertItem(response);
        inOrder.verify(userProfilePictureCacheRepository, times(1)).get(any());
        inOrder.verify(profilePictureRepository, times(1)).listByUserPseudo(any(), any());
        inOrder.verify(userProfilePictureCacheRepository, times(1)).store(any(), any(List.class));
        inOrder.verify(testResponseTransformer).toResponse(any(List.class));
        verifyNoMoreInteractions(testResponseTransformer, userProfilePictureCacheRepository, userProfilePictureCacheRepository);
    }

    @Test
    public void should_handle_profile_picture_repository_exception() {
        // Given
        doReturn(Uni.createFrom().nullItem()).when(userProfilePictureCacheRepository).get(any());
        final ProfilePictureRepositoryException profilePictureRepositoryException = mock(ProfilePictureRepositoryException.class);
        doReturn(Uni.createFrom().failure(profilePictureRepositoryException)).when(profilePictureRepository).listByUserPseudo(any(), any());
        final Response response = mock(Response.class);
        doReturn(response).when(testResponseTransformer).toResponse(profilePictureRepositoryException);

        // When
        final UniAssertSubscriber<Response> subscriber = listUserProfilPicturesUseCase.execute(
                new ListUserProfilPicturesCommand(mock(UserPseudo.class), mock(SupportedMediaType.class)),
                testResponseTransformer)
                .subscribe().withSubscriber(UniAssertSubscriber.create());

        // Then
        subscriber.assertCompleted().assertItem(response);
        verify(userProfilePictureCacheRepository, times(1)).get(any());
        verify(profilePictureRepository, times(1)).listByUserPseudo(any(), any());
        verify(testResponseTransformer).toResponse(any(ProfilePictureRepositoryException.class));
        verifyNoMoreInteractions(testResponseTransformer, userProfilePictureCacheRepository, userProfilePictureCacheRepository);
    }

    @Test
    public void should_handle_runtime_exception() {
        // Given
        doReturn(Uni.createFrom().nullItem()).when(userProfilePictureCacheRepository).get(any());
        final RuntimeException runtimeException = new RuntimeException();
        doReturn(Uni.createFrom().failure(runtimeException)).when(profilePictureRepository).listByUserPseudo(any(), any());
        final Response response = mock(Response.class);
        doReturn(response).when(testResponseTransformer).toResponse(runtimeException);

        // When
        final UniAssertSubscriber<Response> subscriber = listUserProfilPicturesUseCase.execute(
                new ListUserProfilPicturesCommand(mock(UserPseudo.class), mock(SupportedMediaType.class)),
                testResponseTransformer)
                .subscribe().withSubscriber(UniAssertSubscriber.create());

        // Then
        subscriber.assertCompleted().assertItem(response);
        verify(userProfilePictureCacheRepository, times(1)).get(any());
        verify(profilePictureRepository, times(1)).listByUserPseudo(any(), any());
        verify(testResponseTransformer).toResponse(any(RuntimeException.class));
        verifyNoMoreInteractions(testResponseTransformer, userProfilePictureCacheRepository, userProfilePictureCacheRepository);
    }

    @Test
    public void should_get_featured_user_profile_picture_when_cache_is_down() {
        // Given
        final UserPseudo userPseudo = mock(UserPseudo.class);
        final SupportedMediaType supportedMediaType = mock(SupportedMediaType.class);
        final List<ProfilePictureIdentifier> profilePictureIdentifiers = Collections.emptyList();
        doReturn(Uni.createFrom().failure(RuntimeException::new)).when(userProfilePictureCacheRepository).get(userPseudo);
        doReturn(Uni.createFrom().item(profilePictureIdentifiers)).when(profilePictureRepository).listByUserPseudo(userPseudo, supportedMediaType);
        doReturn(Uni.createFrom().failure(RuntimeException::new)).when(userProfilePictureCacheRepository).store(userPseudo, profilePictureIdentifiers);

        final Response response = mock(Response.class);
        doReturn(response).when(testResponseTransformer).toResponse(profilePictureIdentifiers);
        final InOrder inOrder = inOrder(testResponseTransformer, profilePictureRepository, userProfilePictureCacheRepository);

        // When
        final UniAssertSubscriber<Response> subscriber = listUserProfilPicturesUseCase.execute(
                new ListUserProfilPicturesCommand(userPseudo, supportedMediaType),
                testResponseTransformer)
                .subscribe().withSubscriber(UniAssertSubscriber.create());

        // Then
        subscriber.assertCompleted().assertItem(response);
        inOrder.verify(userProfilePictureCacheRepository, times(1)).get(any());
        inOrder.verify(profilePictureRepository, times(1)).listByUserPseudo(any(), any());
        inOrder.verify(userProfilePictureCacheRepository, times(1)).store(any(), any(List.class));
        inOrder.verify(testResponseTransformer).toResponse(any(List.class));
        verifyNoMoreInteractions(testResponseTransformer, userProfilePictureCacheRepository, userProfilePictureCacheRepository);
    }

}
