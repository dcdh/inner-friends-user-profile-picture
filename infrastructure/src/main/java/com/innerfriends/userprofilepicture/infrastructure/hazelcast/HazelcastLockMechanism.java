package com.innerfriends.userprofilepicture.infrastructure.hazelcast;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.cp.lock.FencedLock;
import com.innerfriends.userprofilepicture.domain.LockMechanism;
import com.innerfriends.userprofilepicture.domain.UserPseudo;
import com.innerfriends.userprofilepicture.infrastructure.tracing.OpenTelemetryTracingService;
import io.opentelemetry.api.trace.Span;
import io.smallrye.mutiny.Uni;

import javax.enterprise.context.ApplicationScoped;
import java.util.Objects;

@ApplicationScoped
public class HazelcastLockMechanism implements LockMechanism {

    private final HazelcastInstance hazelcastInstance;
    private final OpenTelemetryTracingService openTelemetryTracingService;

    public HazelcastLockMechanism(final HazelcastInstance hazelcastInstance,
                                  final OpenTelemetryTracingService openTelemetryTracingService) {
        this.hazelcastInstance = Objects.requireNonNull(hazelcastInstance);
        this.openTelemetryTracingService = Objects.requireNonNull(openTelemetryTracingService);
    }

    @Override
    public Uni<Void> lock(final UserPseudo userPseudo) {
        final Span span = openTelemetryTracingService.startANewSpan("HazelcastLockMechanism.lock");
        return Uni.createFrom()
                .item(() -> {
                    hazelcastInstance.getCPSubsystem().getLock(userPseudo.pseudo()).lock();
                    return (Void) null;
                })
                .onTermination()
                .invoke(() -> openTelemetryTracingService.endSpan(span));
    }

    @Override
    public Uni<Void> unlock(final UserPseudo userPseudo) {
        final Span span = openTelemetryTracingService.startANewSpan("HazelcastLockMechanism.unlock");
        return Uni.createFrom()
                .item(() -> {
                    final FencedLock lock = hazelcastInstance.getCPSubsystem().getLock(userPseudo.pseudo());
                    if (lock.isLockedByCurrentThread()) {
                        lock.unlock();
                    }
                    return (Void) null;
                })
                .onTermination()
                .invoke(() -> openTelemetryTracingService.endSpan(span));
    }
}
