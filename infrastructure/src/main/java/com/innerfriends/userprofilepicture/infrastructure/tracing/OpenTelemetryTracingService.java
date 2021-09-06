package com.innerfriends.userprofilepicture.infrastructure.tracing;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;

import javax.enterprise.context.ApplicationScoped;
import java.util.Objects;

@ApplicationScoped
public class OpenTelemetryTracingService {

    private final Tracer tracer;

    public OpenTelemetryTracingService(final Tracer tracer) {
        this.tracer = Objects.requireNonNull(tracer);
    }

    public Span startANewSpan(final String spanName) {
        return tracer.spanBuilder(spanName)
                .startSpan();
    }

    public void endSpan(final Span span) {
        span.end();
    }

    public void markSpanInError(final Span span) {
        span.setStatus(StatusCode.ERROR);
    }

}
