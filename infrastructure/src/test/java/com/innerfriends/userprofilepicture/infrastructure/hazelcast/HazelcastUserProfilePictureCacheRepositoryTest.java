package com.innerfriends.userprofilepicture.infrastructure.hazelcast;

import com.hazelcast.core.HazelcastInstance;
import com.innerfriends.userprofilepicture.domain.*;
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
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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
        final CachedUserProfilePictures givenCachedUserProfilePictures = HazelcastCachedUserProfilePictures.newBuilder()
                .setUserPseudo("user")
                .setFeaturedUserProfilePictureIdentifier(
                        HazelcastUserProfilePictureIdentifier.newBuilder()
                                .setUserPseudo("user")
                                .setMediaType(SupportedMediaType.IMAGE_JPEG)
                                .setVersionId("v0").build())
                .addProfilePictureIdentifier(
                        HazelcastUserProfilePictureIdentifier.newBuilder()
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
                .setFeaturedUserProfilePictureIdentifier(
                        HazelcastUserProfilePictureIdentifier.newBuilder()
                                .setUserPseudo("user")
                                .setMediaType(SupportedMediaType.IMAGE_JPEG)
                                .setVersionId("v0").build())
                .addProfilePictureIdentifier(
                        HazelcastUserProfilePictureIdentifier.newBuilder()
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
        final InOrder inOrder = inOrder(openTelemetryTracingService);
        final Span span = mock(Span.class);
        doReturn(span).when(openTelemetryTracingService).startANewSpan(any());

        // When
        final Uni<CachedUserProfilePictures> uni = hazelcastUserProfilePictureCacheRepository.get(() -> "user");

        // Then
        final UniAssertSubscriber<CachedUserProfilePictures> subscriber = uni.subscribe().withSubscriber(UniAssertSubscriber.create());
        subscriber.awaitFailure().assertFailedWith(UserProfilePicturesNotInCacheException.class);
        inOrder.verify(openTelemetryTracingService, atLeast(1)).startANewSpan(any());
        // I cannot do @InjectSpy on hazelcastInstance to verify the call in order
        inOrder.verify(openTelemetryTracingService, times(1)).endSpan(span);

        assertThat(hazelcastInstance.getMap(HazelcastUserProfilePictureCacheRepository.MAP_NAME).get("user")).isNull();
    }

    @Test
    public void should_store_featured_profile_picture_when_user_profile_picture_in_cache() {
        // Given
        final CachedUserProfilePictures givenCachedUserProfilePictures = HazelcastCachedUserProfilePictures.newBuilder()
                .setUserPseudo("user")
                .build();
        hazelcastInstance.getMap(HazelcastUserProfilePictureCacheRepository.MAP_NAME).put("user", givenCachedUserProfilePictures);
        final InOrder inOrder = inOrder(openTelemetryTracingService);
        final Span span = mock(Span.class);
        doReturn(span).when(openTelemetryTracingService).startANewSpan(any());

        // When
        final Uni<CachedUserProfilePictures> uni = hazelcastUserProfilePictureCacheRepository.storeFeatured(() -> "user",
                HazelcastUserProfilePictureIdentifier.newBuilder()
                        .setUserPseudo("user")
                        .setMediaType(SupportedMediaType.IMAGE_JPEG)
                        .setVersionId("v0").build());

        // Then
        final UniAssertSubscriber<CachedUserProfilePictures> subscriber = uni.subscribe().withSubscriber(UniAssertSubscriber.create());
        final CachedUserProfilePictures cachedUserProfilePictures = subscriber.awaitItem().assertCompleted().getItem();
        final HazelcastCachedUserProfilePictures expectedHazelcastCachedUserProfilePicture = HazelcastCachedUserProfilePictures.newBuilder()
                .setUserPseudo("user")
                .setFeaturedUserProfilePictureIdentifier(
                        HazelcastUserProfilePictureIdentifier.newBuilder()
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
    public void should_store_featured_profile_picture_when_user_profile_picture_not_in_cache() {
        // Given
        final InOrder inOrder = inOrder(openTelemetryTracingService);
        final Span span = mock(Span.class);
        doReturn(span).when(openTelemetryTracingService).startANewSpan(any());

        // When
        final Uni<CachedUserProfilePictures> uni = hazelcastUserProfilePictureCacheRepository.storeFeatured(() -> "user",
                HazelcastUserProfilePictureIdentifier.newBuilder()
                        .setUserPseudo("user")
                        .setMediaType(SupportedMediaType.IMAGE_JPEG)
                        .setVersionId("v0").build());

        // Then
        final UniAssertSubscriber<CachedUserProfilePictures> subscriber = uni.subscribe().withSubscriber(UniAssertSubscriber.create());
        final CachedUserProfilePictures cachedUserProfilePictures = subscriber.awaitItem().assertCompleted().getItem();
        final HazelcastCachedUserProfilePictures expectedHazelcastCachedUserProfilePicture = HazelcastCachedUserProfilePictures.newBuilder()
                .setUserPseudo("user")
                .setFeaturedUserProfilePictureIdentifier(
                        HazelcastUserProfilePictureIdentifier.newBuilder()
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
    public void should_not_store_profile_picture_identifier_when_cannot_be_stored() {
        // Given
        final UserProfilePictures userProfilePictures = mock(UserProfilePictures.class);
        doReturn(false).when(userProfilePictures).canBeStoredInCache();

        // When
        final Uni<CachedUserProfilePictures> uni = hazelcastUserProfilePictureCacheRepository.store(() -> "user", userProfilePictures);

        // Then
        final UniAssertSubscriber<CachedUserProfilePictures> subscriber = uni.subscribe().withSubscriber(UniAssertSubscriber.create());
        assertThat(subscriber.awaitItem().assertCompleted().getItem()).isEqualTo(null);
        assertThat(hazelcastInstance.getMap(HazelcastUserProfilePictureCacheRepository.MAP_NAME).get("user")).isNull();
        verify(userProfilePictures, times(1)).canBeStoredInCache();
    }

    @Test
    public void should_store_profile_picture_identifier_when_user_profile_picture_not_in_cache() {
        // Given
        final UserProfilePictures userProfilePictures = mock(UserProfilePictures.class);
        doReturn(true).when(userProfilePictures).canBeStoredInCache();
        doReturn(buildUserProfilePicture(3)).when(userProfilePictures).userProfilePictures();
        final InOrder inOrder = inOrder(userProfilePictures, openTelemetryTracingService);
        final Span span = mock(Span.class);
        doReturn(span).when(openTelemetryTracingService).startANewSpan(any());

        // When
        final Uni<CachedUserProfilePictures> uni = hazelcastUserProfilePictureCacheRepository.store(() -> "user", userProfilePictures);

        // Then
        final UniAssertSubscriber<CachedUserProfilePictures> subscriber = uni.subscribe().withSubscriber(UniAssertSubscriber.create());
        final HazelcastCachedUserProfilePictures expectedHazelcastCachedUserProfilePicture = HazelcastCachedUserProfilePictures.newBuilder()
                .setUserPseudo("user")
                .addProfilePictureIdentifier(
                        HazelcastUserProfilePictureIdentifier.newBuilder()
                                .setUserPseudo("user")
                                .setMediaType(SupportedMediaType.IMAGE_JPEG)
                                .setVersionId("v0").build())
                .addProfilePictureIdentifier(
                        HazelcastUserProfilePictureIdentifier.newBuilder()
                                .setUserPseudo("user")
                                .setMediaType(SupportedMediaType.IMAGE_JPEG)
                                .setVersionId("v1").build())
                .addProfilePictureIdentifier(
                        HazelcastUserProfilePictureIdentifier.newBuilder()
                                .setUserPseudo("user")
                                .setMediaType(SupportedMediaType.IMAGE_JPEG)
                                .setVersionId("v2").build())
                .build();
        assertThat(subscriber.awaitItem().assertCompleted().getItem()).isEqualTo(expectedHazelcastCachedUserProfilePicture);
        assertThat(hazelcastInstance.getMap(HazelcastUserProfilePictureCacheRepository.MAP_NAME).get("user")).isEqualTo(expectedHazelcastCachedUserProfilePicture);
        inOrder.verify(userProfilePictures, times(1)).canBeStoredInCache();
        inOrder.verify(openTelemetryTracingService, atLeast(1)).startANewSpan(any());
        // I cannot do @InjectSpy on hazelcastInstance to verify the call in order
        inOrder.verify(openTelemetryTracingService, times(1)).endSpan(span);
    }

    @Test
    public void should_store_profile_picture_identifier_when_user_profile_picture_in_cache() {
        // Given
        final UserProfilePictures userProfilePictures = mock(UserProfilePictures.class);
        doReturn(true).when(userProfilePictures).canBeStoredInCache();
        doReturn(buildUserProfilePicture(1)).when(userProfilePictures).userProfilePictures();
        final HazelcastCachedUserProfilePictures givenHazelcastCachedUserProfilePicture = HazelcastCachedUserProfilePictures.newBuilder()
                .setUserPseudo("user")
                .addProfilePictureIdentifier(
                        HazelcastUserProfilePictureIdentifier.newBuilder()
                                .setUserPseudo("user")
                                .setMediaType(SupportedMediaType.IMAGE_JPEG)
                                .setVersionId("v0").build())
                .addProfilePictureIdentifier(
                        HazelcastUserProfilePictureIdentifier.newBuilder()
                                .setUserPseudo("user")
                                .setMediaType(SupportedMediaType.IMAGE_JPEG)
                                .setVersionId("v1").build())
                .addProfilePictureIdentifier(
                        HazelcastUserProfilePictureIdentifier.newBuilder()
                                .setUserPseudo("user")
                                .setMediaType(SupportedMediaType.IMAGE_JPEG)
                                .setVersionId("v2").build())
                .build();
        hazelcastInstance.getMap(HazelcastUserProfilePictureCacheRepository.MAP_NAME).put("user", givenHazelcastCachedUserProfilePicture);
        final InOrder inOrder = inOrder(userProfilePictures, openTelemetryTracingService);
        final Span span = mock(Span.class);
        doReturn(span).when(openTelemetryTracingService).startANewSpan(any());

        // When
        final Uni<CachedUserProfilePictures> uni = hazelcastUserProfilePictureCacheRepository.store(() -> "user", userProfilePictures);

        // Then
        final UniAssertSubscriber<CachedUserProfilePictures> subscriber = uni.subscribe().withSubscriber(UniAssertSubscriber.create());
        final HazelcastCachedUserProfilePictures expectedHazelcastCachedUserProfilePicture = HazelcastCachedUserProfilePictures.newBuilder()
                .setUserPseudo("user")
                .addProfilePictureIdentifier(
                        HazelcastUserProfilePictureIdentifier.newBuilder()
                                .setUserPseudo("user")
                                .setMediaType(SupportedMediaType.IMAGE_JPEG)
                                .setVersionId("v0").build())
                .build();
        assertThat(subscriber.awaitItem().assertCompleted().getItem()).isEqualTo(expectedHazelcastCachedUserProfilePicture);
        assertThat(hazelcastInstance.getMap(HazelcastUserProfilePictureCacheRepository.MAP_NAME).get("user")).isEqualTo(expectedHazelcastCachedUserProfilePicture);
        inOrder.verify(userProfilePictures, times(1)).canBeStoredInCache();
        inOrder.verify(openTelemetryTracingService, atLeast(1)).startANewSpan(any());
        // I cannot do @InjectSpy on hazelcastInstance to verify the call in order
        inOrder.verify(openTelemetryTracingService, times(1)).endSpan(span);
    }

    @Test
    public void should_evict_user_profile_pictures() {
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
        doReturn(span).when(openTelemetryTracingService).startANewSpan("HazelcastUserProfilePictureCacheRepository.evict");

        // When
        final Uni<Void> uni = hazelcastUserProfilePictureCacheRepository.evict(() -> "user");

        // Then
        final UniAssertSubscriber<Void> subscriber = uni.subscribe().withSubscriber(UniAssertSubscriber.create());
        subscriber.awaitItem().assertCompleted();

        assertThat(hazelcastInstance.getMap(HazelcastUserProfilePictureCacheRepository.MAP_NAME).get("user")).isNull();

        inOrder.verify(openTelemetryTracingService, atLeast(1)).startANewSpan(any());
        // I cannot do @InjectSpy on hazelcastInstance to verify the call in order
        inOrder.verify(openTelemetryTracingService, times(1)).endSpan(span);
    }

    private List<UserProfilePicture> buildUserProfilePicture(final int nbOfPictures) {
        return IntStream.range(0, nbOfPictures)
                .boxed()
                .map(index -> new TestUserProfilePictureIdentifier("v" + index))
                .collect(Collectors.toList());
    }

    private final class TestVersionId implements VersionId {

        private final String versionId;

        public TestVersionId(final String versionId) {
            this.versionId = versionId;
        }

        @Override
        public String version() {
            return versionId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof TestVersionId)) return false;
            TestVersionId that = (TestVersionId) o;
            return Objects.equals(versionId, that.versionId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(versionId);
        }
    }

    private final class TestUserProfilePictureIdentifier implements UserProfilePicture {

        private final VersionId versionId;

        public TestUserProfilePictureIdentifier(final String versionId) {
            this.versionId = new TestVersionId(versionId);
        }

        @Override
        public UserPseudo userPseudo() {
            return () -> "user";
        }

        @Override
        public SupportedMediaType mediaType() {
            return SupportedMediaType.IMAGE_JPEG;
        }

        @Override
        public VersionId versionId() {
            return versionId;
        }

        @Override
        public boolean isFeatured() {
            return false;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof TestUserProfilePictureIdentifier)) return false;
            TestUserProfilePictureIdentifier that = (TestUserProfilePictureIdentifier) o;
            return Objects.equals(versionId, that.versionId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(versionId);
        }

    }

}
