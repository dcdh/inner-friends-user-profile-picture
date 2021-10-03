package com.innerfriends.userprofilepicture.infrastructure.hazelcast;

import com.hazelcast.core.HazelcastInstance;
import com.innerfriends.userprofilepicture.infrastructure.tracing.OpenTelemetryTracingService;
import io.opentelemetry.api.trace.Span;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import javax.inject.Inject;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@QuarkusTest
public class HazelcastLockMechanismTest {

    @Inject
    HazelcastLockMechanism hazelcastLockMechanism;

    @Inject
    HazelcastInstance hazelcastInstance;

    @InjectMock
    OpenTelemetryTracingService openTelemetryTracingService;

    @Test
    public void should_lock() throws Exception {
        // Given
        final Span span = mock(Span.class);
        doReturn(span).when(openTelemetryTracingService).startANewSpan("HazelcastLockMechanism.lock");
        final InOrder inOrder = inOrder(openTelemetryTracingService);

        // When
        hazelcastLockMechanism.lock(() -> "pseudoToLock").await().atMost(Duration.ofSeconds(1l));

        // Then
        assertThat(hazelcastInstance.getCPSubsystem().getLock("pseudoToLock").isLocked()).isTrue();
        inOrder.verify(openTelemetryTracingService, atLeast(1)).startANewSpan(any());
        // I cannot do @InjectSpy on hazelcastInstance to verify the call in order
        inOrder.verify(openTelemetryTracingService, times(1)).endSpan(span);
        hazelcastInstance.getCPSubsystem().getLock("pseudoToLock").unlock();
    }

    @Test
    public void should_unlock() throws Exception {
        // Given
        final Span span = mock(Span.class);
        doReturn(span).when(openTelemetryTracingService).startANewSpan(any());
        final InOrder inOrder = inOrder(openTelemetryTracingService);
        hazelcastInstance.getCPSubsystem().getLock("pseudoToLock").lock();

        // When
        hazelcastLockMechanism.unlock(() -> "pseudoToLock").await().atMost(Duration.ofSeconds(1l));

        // Then
        assertThat(hazelcastInstance.getCPSubsystem().getLock("pseudoToLock").isLocked()).isFalse();
        inOrder.verify(openTelemetryTracingService, atLeast(1)).startANewSpan(any());
        // I cannot do @InjectSpy on hazelcastInstance to verify the call in order
        inOrder.verify(openTelemetryTracingService, times(1)).endSpan(span);
    }

}
