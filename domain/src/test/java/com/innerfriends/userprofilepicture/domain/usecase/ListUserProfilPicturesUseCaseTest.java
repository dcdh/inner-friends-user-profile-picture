package com.innerfriends.userprofilepicture.domain.usecase;

import com.innerfriends.userprofilepicture.domain.*;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.helpers.test.UniAssertSubscriber;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verifyNoMoreInteractions;

public class ListUserProfilPicturesUseCaseTest {

    @Test
    public void should_get_featured_user_profile_picture() {
        // Given
        final ProfilePictureRepository profilePictureRepository = mock(ProfilePictureRepository.class);
        final ListUserProfilPicturesUseCase<Response> listUserProfilPicturesUseCase = new ListUserProfilPicturesUseCase<>(profilePictureRepository);
        final TestResponseTransformer testResponseTransformer = mock(TestResponseTransformer.class);
        final UserPseudo userPseudo = mock(UserPseudo.class);
        final SupportedMediaType supportedMediaType = mock(SupportedMediaType.class);
        final List<ProfilePictureIdentifier> profilePictureIdentifiers = Collections.emptyList();
        doReturn(Uni.createFrom().item(profilePictureIdentifiers)).when(profilePictureRepository).listByUserPseudo(userPseudo, supportedMediaType);
        final Response response = mock(Response.class);
        doReturn(response).when(testResponseTransformer).toResponse(profilePictureIdentifiers);

        // When
        final UniAssertSubscriber<Response> subscriber = listUserProfilPicturesUseCase.execute(
                new ListUserProfilPicturesCommand(userPseudo, supportedMediaType),
                testResponseTransformer)
                .subscribe().withSubscriber(UniAssertSubscriber.create());

        // Then
        subscriber.assertCompleted().assertItem(response);
        verify(profilePictureRepository, times(1)).listByUserPseudo(any(), any());
        verify(testResponseTransformer).toResponse(any(List.class));
        verifyNoMoreInteractions(testResponseTransformer);
    }

    @Test
    public void should_handle_profile_picture_repository_exception() {
        // Given
        final ProfilePictureRepository profilePictureRepository = mock(ProfilePictureRepository.class);
        final ListUserProfilPicturesUseCase<Response> listUserProfilPicturesUseCase = new ListUserProfilPicturesUseCase<>(profilePictureRepository);
        final TestResponseTransformer testResponseTransformer = mock(TestResponseTransformer.class);
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
        verify(profilePictureRepository, times(1)).listByUserPseudo(any(), any());
        verify(testResponseTransformer).toResponse(any(ProfilePictureRepositoryException.class));
        verifyNoMoreInteractions(testResponseTransformer);
    }

    @Test
    public void should_handle_runtime_exception() {
        // Given
        final ProfilePictureRepository profilePictureRepository = mock(ProfilePictureRepository.class);
        final ListUserProfilPicturesUseCase<Response> listUserProfilPicturesUseCase = new ListUserProfilPicturesUseCase<>(profilePictureRepository);
        final TestResponseTransformer testResponseTransformer = mock(TestResponseTransformer.class);
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
        verify(profilePictureRepository, times(1)).listByUserPseudo(any(), any());
        verify(testResponseTransformer).toResponse(any(RuntimeException.class));
        verifyNoMoreInteractions(testResponseTransformer);
    }

}
