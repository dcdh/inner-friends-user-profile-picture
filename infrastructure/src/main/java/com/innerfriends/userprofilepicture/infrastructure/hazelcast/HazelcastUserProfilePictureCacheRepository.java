package com.innerfriends.userprofilepicture.infrastructure.hazelcast;

import com.hazelcast.core.HazelcastInstance;
import com.innerfriends.userprofilepicture.domain.*;
import com.innerfriends.userprofilepicture.infrastructure.tracing.OpenTelemetryTracingService;
import io.opentelemetry.api.trace.Span;
import io.smallrye.mutiny.Uni;

import javax.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@ApplicationScoped
public class HazelcastUserProfilePictureCacheRepository implements UserProfilePictureCacheRepository {

    public static final String MAP_NAME = "userProfilePicture";

    private final HazelcastInstance hazelcastInstance;
    private final OpenTelemetryTracingService openTelemetryTracingService;

    public HazelcastUserProfilePictureCacheRepository(final HazelcastInstance hazelcastInstance,
                                                      final OpenTelemetryTracingService openTelemetryTracingService) {
        this.hazelcastInstance = Objects.requireNonNull(hazelcastInstance);
        this.openTelemetryTracingService = Objects.requireNonNull(openTelemetryTracingService);
    }

    /**
     * Warning
     * The putAsync operation returns the old value as result.
     * I should map the response and returned the new value passed as parameter.
     * I may face cache propagation issue when requesting values present in the store.
     */

    @Override
    public Uni<CachedUserProfilePictures> get(final UserPseudo userPseudo) throws UserProfileNotInCacheException {
        return Uni.createFrom()
                .deferred(() -> {
                    final Span span = openTelemetryTracingService.startANewSpan("HazelcastUserProfilePictureCacheRepository.get");
                    return Uni.createFrom()
                            .completionStage(() -> hazelcastInstance.getMap(MAP_NAME).getAsync(userPseudo.pseudo()))
                            .map(cachedUserProfilePicture -> cachedUserProfilePicture)
                            .onItem()
                            .castTo(CachedUserProfilePictures.class)
                            .replaceIfNullWith(() -> {
                                throw new UserProfilePicturesNotInCacheException(userPseudo);
                            })
                            .onTermination()
                            .invoke(() -> openTelemetryTracingService.endSpan(span));
                });
    }

    @Override
    public Uni<CachedUserProfilePictures> store(final UserPseudo userPseudo, final List<? extends UserProfilePictureIdentifier> userProfilePictureIdentifiers) {
        return Uni.createFrom()
                .deferred(() -> {
                    final Span span = openTelemetryTracingService.startANewSpan("HazelcastUserProfilePictureCacheRepository.store");
                    return Uni.createFrom()
                            .completionStage(() -> hazelcastInstance.getMap(MAP_NAME).getAsync(userPseudo.pseudo()))
                            .replaceIfNullWith(() -> HazelcastCachedUserProfilePictures.newBuilder().setUserPseudo(userPseudo.pseudo()).build())
                            .onItem().castTo(HazelcastCachedUserProfilePictures.class)
                            .chain(hazelcastCachedUserProfilePicture -> {
                                hazelcastCachedUserProfilePicture.replaceAllProfilePictureIdentifiers(
                                        userProfilePictureIdentifiers.stream()
                                                .map(HazelcastUserProfilePictureIdentifier::new)
                                                .collect(Collectors.toList())
                                );
                                return Uni.createFrom().completionStage(() -> hazelcastInstance.getMap(MAP_NAME).putAsync(userPseudo.pseudo(), hazelcastCachedUserProfilePicture))
                                        .map(response -> hazelcastCachedUserProfilePicture);
                            })
                            .onItem().castTo(HazelcastCachedUserProfilePictures.class)
                            .onTermination()
                            .invoke(() -> openTelemetryTracingService.endSpan(span));
                });
    }

    @Override
    public Uni<CachedUserProfilePictures> storeFeatured(final UserPseudo userPseudo, final UserProfilePictureIdentifier featured) {
        return Uni.createFrom()
                .deferred(() -> {
                    final Span span = openTelemetryTracingService.startANewSpan("HazelcastUserProfilePictureCacheRepository.store");
                    return Uni.createFrom()
                            .completionStage(() -> hazelcastInstance.getMap(MAP_NAME).getAsync(userPseudo.pseudo()))
                            .replaceIfNullWith(() -> HazelcastCachedUserProfilePictures.newBuilder().setUserPseudo(userPseudo.pseudo()).build())
                            .onItem().castTo(HazelcastCachedUserProfilePictures.class)
                            .chain(hazelcastCachedUserProfilePicture -> {
                                hazelcastCachedUserProfilePicture.setFeaturedUserProfilePictureIdentifier(new HazelcastUserProfilePictureIdentifier(featured));
                                return Uni.createFrom().completionStage(() -> hazelcastInstance.getMap(MAP_NAME).putAsync(userPseudo.pseudo(), hazelcastCachedUserProfilePicture))
                                        .map(response -> hazelcastCachedUserProfilePicture);
                            })
                            .onItem().castTo(HazelcastCachedUserProfilePictures.class)
                            .onTermination()
                            .invoke(() -> openTelemetryTracingService.endSpan(span));
                });
    }

    @Override
    public Uni<Void> evict(final UserPseudo userPseudo) {
        return Uni.createFrom()
                .deferred(() -> {
                    final Span span = openTelemetryTracingService.startANewSpan("HazelcastUserProfilePictureCacheRepository.evict");
                    return Uni.createFrom()
                            .completionStage(() -> hazelcastInstance.getMap(MAP_NAME).removeAsync(userPseudo.pseudo()))
                            .onItemOrFailure()
                            .transform((item, exception) -> (Void) null)
                            .onTermination()
                            .invoke(() -> openTelemetryTracingService.endSpan(span));
                });
    }

}
