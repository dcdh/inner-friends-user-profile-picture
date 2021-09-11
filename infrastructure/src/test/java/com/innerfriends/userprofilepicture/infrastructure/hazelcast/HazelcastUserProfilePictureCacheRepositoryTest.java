package com.innerfriends.userprofilepicture.infrastructure.hazelcast;

import com.hazelcast.core.HazelcastInstance;
import com.innerfriends.userprofilepicture.domain.CachedUserProfilePictures;
import com.innerfriends.userprofilepicture.domain.SupportedMediaType;
import com.innerfriends.userprofilepicture.domain.UserProfilePictureNotInCacheException;
import com.innerfriends.userprofilepicture.infrastructure.tracing.OpenTelemetryTracingService;
import io.opentelemetry.api.trace.Span;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.helpers.test.UniAssertSubscriber;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import javax.inject.Inject;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@QuarkusTest
public class HazelcastUserProfilePictureCacheRepositoryTest {

    @Inject
    HazelcastUserProfilePictureCacheRepository hazelcastUserProfilePictureCacheRepository;

    @Inject
    HazelcastInstance hazelcastInstance;

    @InjectMock
    OpenTelemetryTracingService openTelemetryTracingService;

    @BeforeEach
    @AfterEach
    public void flush() {
        hazelcastInstance.getMap(HazelcastUserProfilePictureCacheRepository.MAP_NAME).clear();
    }

    @Test
    public void should_get_return_stored_cached_user_profile_picture() {
        // Given
        if (hazelcastInstance.getMap(HazelcastUserProfilePictureCacheRepository.MAP_NAME).get("user") != null) {
            throw new IllegalStateException("must be null !");
        }
        final CachedUserProfilePictures givenCachedUserProfilePictures = HazelcastCachedUserProfilePictures.newBuilder()
                .setUserPseudo("user")
                .setFeaturedProfilePictureIdentifier(
                        HazelcastProfilePictureIdentifier.newBuilder()
                                .setUserPseudo("user")
                                .setMediaType(SupportedMediaType.IMAGE_JPEG)
                                .setVersionId("v0").build())
                .addProfilePictureIdentifier(
                        HazelcastProfilePictureIdentifier.newBuilder()
                                .setUserPseudo("user")
                                .setMediaType(SupportedMediaType.IMAGE_JPEG)
                                .setVersionId("v0").build())
                .build();
        hazelcastInstance.getMap(HazelcastUserProfilePictureCacheRepository.MAP_NAME).put("user", givenCachedUserProfilePictures);
        final InOrder inOrder = inOrder(openTelemetryTracingService);
        final Span span = mock(Span.class);
        doReturn(span).when(openTelemetryTracingService).startANewSpan(any());

        // When
        final Uni<CachedUserProfilePictures> uni = hazelcastUserProfilePictureCacheRepository.get(() -> "user");

        // Then
        final UniAssertSubscriber<CachedUserProfilePictures> subscriber = uni.subscribe().withSubscriber(UniAssertSubscriber.create());
        final CachedUserProfilePictures cachedUserProfilePictures = subscriber.awaitItem().assertCompleted().getItem();
        assertThat(cachedUserProfilePictures).isEqualTo(HazelcastCachedUserProfilePictures.newBuilder()
                .setUserPseudo("user")
                .setFeaturedProfilePictureIdentifier(
                        HazelcastProfilePictureIdentifier.newBuilder()
                                .setUserPseudo("user")
                                .setMediaType(SupportedMediaType.IMAGE_JPEG)
                                .setVersionId("v0").build())
                .addProfilePictureIdentifier(
                        HazelcastProfilePictureIdentifier.newBuilder()
                                .setUserPseudo("user")
                                .setMediaType(SupportedMediaType.IMAGE_JPEG)
                                .setVersionId("v0").build())
                .build());
        inOrder.verify(openTelemetryTracingService, atLeast(1)).startANewSpan(any());
        // I cannot do @InjectSpy on hazelcastInstance to verify the call in order
        inOrder.verify(openTelemetryTracingService, times(1)).endSpan(span);
    }

    @Test
    public void should_throw_user_profile_picture_not_in_cache_exception_when_user_profile_picture_not_in_cache() {
        // Given
        if (hazelcastInstance.getMap(HazelcastUserProfilePictureCacheRepository.MAP_NAME).get("user") != null) {
            throw new IllegalStateException("must be null !");
        }
        final InOrder inOrder = inOrder(openTelemetryTracingService);
        final Span span = mock(Span.class);
        doReturn(span).when(openTelemetryTracingService).startANewSpan(any());

        // When
        final Uni<CachedUserProfilePictures> uni = hazelcastUserProfilePictureCacheRepository.get(() -> "user");

        // Then
        final UniAssertSubscriber<CachedUserProfilePictures> subscriber = uni.subscribe().withSubscriber(UniAssertSubscriber.create());
        subscriber.awaitFailure().assertFailedWith(UserProfilePictureNotInCacheException.class);
        inOrder.verify(openTelemetryTracingService, atLeast(1)).startANewSpan(any());
        // I cannot do @InjectSpy on hazelcastInstance to verify the call in order
        inOrder.verify(openTelemetryTracingService, times(1)).markSpanInError(span);
        inOrder.verify(openTelemetryTracingService, times(1)).endSpan(span);

        assertThat(hazelcastInstance.getMap(HazelcastUserProfilePictureCacheRepository.MAP_NAME).get("user")).isNull();
    }

    @Test
    public void should_store_profile_picture_identifiers_when_user_profile_picture_in_cache() {
        // Given
        if (hazelcastInstance.getMap(HazelcastUserProfilePictureCacheRepository.MAP_NAME).get("user") != null) {
            throw new IllegalStateException("must be null !");
        }
        final CachedUserProfilePictures givenCachedUserProfilePictures = HazelcastCachedUserProfilePictures.newBuilder()
                .setUserPseudo("user")
                .build();
        hazelcastInstance.getMap(HazelcastUserProfilePictureCacheRepository.MAP_NAME).put("user", givenCachedUserProfilePictures);
        final InOrder inOrder = inOrder(openTelemetryTracingService);
        final Span span = mock(Span.class);
        doReturn(span).when(openTelemetryTracingService).startANewSpan(any());

        // When
        final Uni<CachedUserProfilePictures> uni = hazelcastUserProfilePictureCacheRepository.storeFeatured(() -> "user",
                HazelcastProfilePictureIdentifier.newBuilder()
                        .setUserPseudo("user")
                        .setMediaType(SupportedMediaType.IMAGE_JPEG)
                        .setVersionId("v0").build());

        // Then
        final UniAssertSubscriber<CachedUserProfilePictures> subscriber = uni.subscribe().withSubscriber(UniAssertSubscriber.create());
        final CachedUserProfilePictures cachedUserProfilePictures = subscriber.awaitItem().assertCompleted().getItem();
        final HazelcastCachedUserProfilePictures expectedHazelcastCachedUserProfilePicture = HazelcastCachedUserProfilePictures.newBuilder()
                .setUserPseudo("user")
                .setFeaturedProfilePictureIdentifier(
                        HazelcastProfilePictureIdentifier.newBuilder()
                                .setUserPseudo("user")
                                .setMediaType(SupportedMediaType.IMAGE_JPEG)
                                .setVersionId("v0").build())
                .build();
        assertThat(cachedUserProfilePictures).isEqualTo(expectedHazelcastCachedUserProfilePicture);
        inOrder.verify(openTelemetryTracingService, atLeast(1)).startANewSpan(any());
        // I cannot do @InjectSpy on hazelcastInstance to verify the call in order
        inOrder.verify(openTelemetryTracingService, times(1)).endSpan(span);

        assertThat(hazelcastInstance.getMap(HazelcastUserProfilePictureCacheRepository.MAP_NAME).get("user"))
                .isEqualTo(expectedHazelcastCachedUserProfilePicture);
    }

    @Test
    public void should_store_profile_picture_identifiers_when_user_profile_picture_not_in_cache() {
        // Given
        if (hazelcastInstance.getMap(HazelcastUserProfilePictureCacheRepository.MAP_NAME).get("user") != null) {
            throw new IllegalStateException("must be null !");
        }
        final InOrder inOrder = inOrder(openTelemetryTracingService);
        final Span span = mock(Span.class);
        doReturn(span).when(openTelemetryTracingService).startANewSpan(any());

        // When
        final Uni<CachedUserProfilePictures> uni = hazelcastUserProfilePictureCacheRepository.storeFeatured(() -> "user",
                HazelcastProfilePictureIdentifier.newBuilder()
                        .setUserPseudo("user")
                        .setMediaType(SupportedMediaType.IMAGE_JPEG)
                        .setVersionId("v0").build());

        // Then
        final UniAssertSubscriber<CachedUserProfilePictures> subscriber = uni.subscribe().withSubscriber(UniAssertSubscriber.create());
        final CachedUserProfilePictures cachedUserProfilePictures = subscriber.awaitItem().assertCompleted().getItem();
        final HazelcastCachedUserProfilePictures expectedHazelcastCachedUserProfilePicture = HazelcastCachedUserProfilePictures.newBuilder()
                .setUserPseudo("user")
                .setFeaturedProfilePictureIdentifier(
                        HazelcastProfilePictureIdentifier.newBuilder()
                                .setUserPseudo("user")
                                .setMediaType(SupportedMediaType.IMAGE_JPEG)
                                .setVersionId("v0").build())
                .build();
        assertThat(cachedUserProfilePictures).isEqualTo(expectedHazelcastCachedUserProfilePicture);
        inOrder.verify(openTelemetryTracingService, atLeast(1)).startANewSpan(any());
        // I cannot do @InjectSpy on hazelcastInstance to verify the call in order
        inOrder.verify(openTelemetryTracingService, times(1)).endSpan(span);

        assertThat(hazelcastInstance.getMap(HazelcastUserProfilePictureCacheRepository.MAP_NAME).get("user"))
                .isEqualTo(expectedHazelcastCachedUserProfilePicture);
    }

    @Test
    public void should_store_featured_profile_picture_identifier_when_user_profile_picture_in_cache() {
        // Given
        if (hazelcastInstance.getMap(HazelcastUserProfilePictureCacheRepository.MAP_NAME).get("user") != null) {
            throw new IllegalStateException("must be null !");
        }
        final CachedUserProfilePictures givenCachedUserProfilePictures = HazelcastCachedUserProfilePictures.newBuilder()
                .setUserPseudo("user")
                .build();
        hazelcastInstance.getMap(HazelcastUserProfilePictureCacheRepository.MAP_NAME).put("user", givenCachedUserProfilePictures);
        final InOrder inOrder = inOrder(openTelemetryTracingService);
        final Span span = mock(Span.class);
        doReturn(span).when(openTelemetryTracingService).startANewSpan(any());

        // When
        final Uni<CachedUserProfilePictures> uni = hazelcastUserProfilePictureCacheRepository.store(() -> "user",
                Collections.singletonList(
                        HazelcastProfilePictureIdentifier.newBuilder()
                                .setUserPseudo("user")
                                .setMediaType(SupportedMediaType.IMAGE_JPEG)
                                .setVersionId("v0").build()));

        // Then
        final UniAssertSubscriber<CachedUserProfilePictures> subscriber = uni.subscribe().withSubscriber(UniAssertSubscriber.create());
        final CachedUserProfilePictures cachedUserProfilePictures = subscriber.awaitItem().assertCompleted().getItem();
        final HazelcastCachedUserProfilePictures expectedHazelcastCachedUserProfilePicture = HazelcastCachedUserProfilePictures.newBuilder()
                .setUserPseudo("user")
                .addProfilePictureIdentifier(
                        HazelcastProfilePictureIdentifier.newBuilder()
                                .setUserPseudo("user")
                                .setMediaType(SupportedMediaType.IMAGE_JPEG)
                                .setVersionId("v0").build())
                .build();
        assertThat(cachedUserProfilePictures).isEqualTo(expectedHazelcastCachedUserProfilePicture);
        inOrder.verify(openTelemetryTracingService, atLeast(1)).startANewSpan(any());
        // I cannot do @InjectSpy on hazelcastInstance to verify the call in order
        inOrder.verify(openTelemetryTracingService, times(1)).endSpan(span);

        assertThat(hazelcastInstance.getMap(HazelcastUserProfilePictureCacheRepository.MAP_NAME).get("user"))
                .isEqualTo(expectedHazelcastCachedUserProfilePicture);
    }

    @Test
    public void should_store_featured_profile_picture_identifier_when_user_profile_picture_not_in_cache() {
        // Given
        if (hazelcastInstance.getMap(HazelcastUserProfilePictureCacheRepository.MAP_NAME).get("user") != null) {
            throw new IllegalStateException("must be null !");
        }
        final InOrder inOrder = inOrder(openTelemetryTracingService);
        final Span span = mock(Span.class);
        doReturn(span).when(openTelemetryTracingService).startANewSpan(any());

        // When
        final Uni<CachedUserProfilePictures> uni = hazelcastUserProfilePictureCacheRepository.store(() -> "user",
                Collections.singletonList(
                        HazelcastProfilePictureIdentifier.newBuilder()
                                .setUserPseudo("user")
                                .setMediaType(SupportedMediaType.IMAGE_JPEG)
                                .setVersionId("v0").build()));

        // Then
        final UniAssertSubscriber<CachedUserProfilePictures> subscriber = uni.subscribe().withSubscriber(UniAssertSubscriber.create());
        final CachedUserProfilePictures cachedUserProfilePictures = subscriber.awaitItem().assertCompleted().getItem();
        final HazelcastCachedUserProfilePictures expectedHazelcastCachedUserProfilePicture = HazelcastCachedUserProfilePictures.newBuilder()
                .setUserPseudo("user")
                .addProfilePictureIdentifier(
                        HazelcastProfilePictureIdentifier.newBuilder()
                                .setUserPseudo("user")
                                .setMediaType(SupportedMediaType.IMAGE_JPEG)
                                .setVersionId("v0").build())
                .build();
        assertThat(cachedUserProfilePictures).isEqualTo(expectedHazelcastCachedUserProfilePicture);
        inOrder.verify(openTelemetryTracingService, atLeast(1)).startANewSpan(any());
        // I cannot do @InjectSpy on hazelcastInstance to verify the call in order
        inOrder.verify(openTelemetryTracingService, times(1)).endSpan(span);

        assertThat(hazelcastInstance.getMap(HazelcastUserProfilePictureCacheRepository.MAP_NAME).get("user"))
                .isEqualTo(expectedHazelcastCachedUserProfilePicture);
    }
}
