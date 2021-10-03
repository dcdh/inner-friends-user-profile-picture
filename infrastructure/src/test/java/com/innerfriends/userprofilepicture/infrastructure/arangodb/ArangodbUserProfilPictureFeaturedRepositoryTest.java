package com.innerfriends.userprofilepicture.infrastructure.arangodb;

import com.arangodb.ArangoDB;
import com.innerfriends.userprofilepicture.domain.*;
import com.innerfriends.userprofilepicture.infrastructure.tracing.OpenTelemetryTracingService;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.helpers.test.UniAssertSubscriber;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.inOrder;

@QuarkusTest
public class ArangodbUserProfilPictureFeaturedRepositoryTest {

    public static final List<String> DELETE_ALL_DOCUMENTS = Arrays.asList("FOR f IN FEATURE REMOVE f IN FEATURE");

    @Inject
    ArangodbUserProfilPictureFeaturedRepository arangodbUserProfilPictureFeaturedRepository;

    @Inject
    ArangoDB arangoDB;

    @ConfigProperty(name = "arangodb.dbName")
    String dbName;

    @InjectMock
    OpenTelemetryTracingService openTelemetryTracingService;

    @BeforeEach
    @AfterEach
    public void flush() {
        DELETE_ALL_DOCUMENTS
                .stream()
                .forEach(query -> arangoDB.db(dbName).query(query, Void.class));
    }

    @Test
    public void should_return_featured_user_profile_picture() {
        // Given
        final String query = "INSERT { _key: \"pseudo\", mediaType : \"IMAGE_JPEG\" , versionId: \"v0\" } INTO FEATURE";
        arangoDB.db(dbName).query(query, Void.class);
        final InOrder inOrder = inOrder(openTelemetryTracingService);

        // When
        final Uni<UserProfilePictureIdentifier> uni = arangodbUserProfilPictureFeaturedRepository.getFeatured(() -> "pseudo");

        // Then
        final UniAssertSubscriber<UserProfilePictureIdentifier> subscriber = uni.subscribe().withSubscriber(UniAssertSubscriber.create());
        final UserProfilePictureIdentifier userProfilePictureIdentifier = subscriber.awaitItem().assertCompleted().getItem();
        assertThat(userProfilePictureIdentifier.userPseudo().pseudo()).isEqualTo("pseudo");
        assertThat(userProfilePictureIdentifier.mediaType()).isEqualTo(SupportedMediaType.IMAGE_JPEG);
        assertThat(userProfilePictureIdentifier.versionId().version()).isEqualTo("v0");
        inOrder.verify(openTelemetryTracingService, atLeast(1)).startANewSpan("ArangodbUserProfilPictureFeaturedRepository.getFeatured");
        inOrder.verify(openTelemetryTracingService, atLeast(1)).endSpan(any());
    }

    @Test
    public void should_return_featured_user_profile_picture_fail_with_no_user_profil_picture_featured_yet_exception_when_user_profile_picture_has_not_be_featured() {
        // Given
        final InOrder inOrder = inOrder(openTelemetryTracingService);

        // When
        final Uni<UserProfilePictureIdentifier> uni = arangodbUserProfilPictureFeaturedRepository.getFeatured(() -> "user");

        // Then
        final UniAssertSubscriber<UserProfilePictureIdentifier> subscriber = uni.subscribe().withSubscriber(UniAssertSubscriber.create());
        subscriber.awaitFailure().assertFailedWith(NoUserProfilPictureFeaturedYetException.class);
        inOrder.verify(openTelemetryTracingService, atLeast(1)).startANewSpan(any());
        inOrder.verify(openTelemetryTracingService, atLeast(1)).endSpan(any());
    }

    @Test
    public void should_return_featured_user_profile_picture_fail_with_user_profil_picture_featured_repository_exception_when_user_pseudo_is_invalid() {
        // Given
        final InOrder inOrder = inOrder(openTelemetryTracingService);

        // When
        final Uni<UserProfilePictureIdentifier> uni = arangodbUserProfilPictureFeaturedRepository.getFeatured(() -> "");

        // Then
        final UniAssertSubscriber<UserProfilePictureIdentifier> subscriber = uni.subscribe().withSubscriber(UniAssertSubscriber.create());
        subscriber.awaitFailure().assertFailedWith(UserProfilPictureFeaturedRepositoryException.class);
        inOrder.verify(openTelemetryTracingService, atLeast(1)).startANewSpan(any());
        inOrder.verify(openTelemetryTracingService, atLeast(1)).markSpanInError(any());
        inOrder.verify(openTelemetryTracingService, atLeast(1)).endSpan(any());
    }

    @Test
    public void should_mark_user_profile_picture_has_featured_when_not_done_previously() {
        // Given
        final InOrder inOrder = inOrder(openTelemetryTracingService);

        // When
        final Uni<UserProfilePictureIdentifier> uni = arangodbUserProfilPictureFeaturedRepository.markAsFeatured(new UserProfilePictureIdentifier() {
            @Override
            public UserPseudo userPseudo() {
                return () -> "pseudo";
            }

            @Override
            public SupportedMediaType mediaType() {
                return SupportedMediaType.IMAGE_JPEG;
            }

            @Override
            public VersionId versionId() {
                return () -> "v0";
            }
        });

        // Then
        final UniAssertSubscriber<UserProfilePictureIdentifier> subscriber = uni.subscribe().withSubscriber(UniAssertSubscriber.create());
        final UserProfilePictureIdentifier userProfilePictureIdentifier = subscriber.awaitItem().assertCompleted().getItem();
        assertThat(userProfilePictureIdentifier.userPseudo().pseudo()).isEqualTo("pseudo");
        assertThat(userProfilePictureIdentifier.mediaType()).isEqualTo(SupportedMediaType.IMAGE_JPEG);
        assertThat(userProfilePictureIdentifier.versionId().version()).isEqualTo("v0");
        inOrder.verify(openTelemetryTracingService, atLeast(1)).startANewSpan("ArangodbUserProfilPictureFeaturedRepository.markAsFeatured");
        inOrder.verify(openTelemetryTracingService, atLeast(1)).endSpan(any());
    }

    @Test
    public void should_mark_user_profile_picture_has_featured_when_done_previously() {
        // Given
        final InOrder inOrder = inOrder(openTelemetryTracingService);
        final String query = "INSERT { _key: \"pseudo\", mediaType : \"IMAGE_JPEG\" , versionId: \"v0\" } INTO FEATURE";
        arangoDB.db(dbName).query(query, Void.class);

        // When
        final Uni<UserProfilePictureIdentifier> uni = arangodbUserProfilPictureFeaturedRepository.markAsFeatured(new UserProfilePictureIdentifier() {
            @Override
            public UserPseudo userPseudo() {
                return () -> "pseudo";
            }

            @Override
            public SupportedMediaType mediaType() {
                return SupportedMediaType.IMAGE_JPEG;
            }

            @Override
            public VersionId versionId() {
                return () -> "v1";
            }
        });

        // Then
        final UniAssertSubscriber<UserProfilePictureIdentifier> subscriber = uni.subscribe().withSubscriber(UniAssertSubscriber.create());
        final UserProfilePictureIdentifier userProfilePictureIdentifier = subscriber.awaitItem().assertCompleted().getItem();
        assertThat(userProfilePictureIdentifier.userPseudo().pseudo()).isEqualTo("pseudo");
        assertThat(userProfilePictureIdentifier.mediaType()).isEqualTo(SupportedMediaType.IMAGE_JPEG);
        assertThat(userProfilePictureIdentifier.versionId().version()).isEqualTo("v1");
        inOrder.verify(openTelemetryTracingService, atLeast(1)).startANewSpan(any());
        inOrder.verify(openTelemetryTracingService, atLeast(1)).endSpan(any());
    }

    @Test
    public void should_mark_user_profile_picture_has_featured_fail_with_user_profil_picture_featured_repository_exception_when_user_pseudo_is_invalid() {
        // Given
        final InOrder inOrder = inOrder(openTelemetryTracingService);

        // When
        final Uni<UserProfilePictureIdentifier> uni = arangodbUserProfilPictureFeaturedRepository.markAsFeatured(new UserProfilePictureIdentifier() {
            @Override
            public UserPseudo userPseudo() {
                return () -> "";
            }

            @Override
            public SupportedMediaType mediaType() {
                return SupportedMediaType.IMAGE_JPEG;
            }

            @Override
            public VersionId versionId() {
                return () -> "v0";
            }
        });

        // Then
        final UniAssertSubscriber<UserProfilePictureIdentifier> subscriber = uni.subscribe().withSubscriber(UniAssertSubscriber.create());
        subscriber.awaitFailure().assertFailedWith(UserProfilPictureFeaturedRepositoryException.class);
        inOrder.verify(openTelemetryTracingService, atLeast(1)).startANewSpan(any());
        inOrder.verify(openTelemetryTracingService, atLeast(1)).markSpanInError(any());
        inOrder.verify(openTelemetryTracingService, atLeast(1)).endSpan(any());
    }

}
