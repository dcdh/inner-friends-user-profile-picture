package com.innerfriends.userprofilepicture.infrastructure.resources;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.wait.strategy.Wait;

import java.util.Map;

public class OpenTelemetryLifecycleManager implements QuarkusTestResourceLifecycleManager {

    private final Logger logger = LoggerFactory.getLogger(OpenTelemetryLifecycleManager.class);

    private Network network;
    private static GenericContainer jaegerTracingAllInOneContainer;
    private GenericContainer otelOpentelemetryCollectorContainer;

    @Override
    public Map<String, String> start() {
        final Slf4jLogConsumer logConsumer = new Slf4jLogConsumer(logger);
        network = Network.newNetwork();
        jaegerTracingAllInOneContainer = new GenericContainer("jaegertracing/all-in-one:1.25.0")
                .withExposedPorts(16686, 14268, 14250)
                .withNetwork(network)
                .withNetworkAliases("jaeger-all-in-one")
                .waitingFor(
                        Wait.forLogMessage(".*Channel Connectivity change to READY.*\\n", 1)
                );
        jaegerTracingAllInOneContainer.start();
        jaegerTracingAllInOneContainer.followOutput(logConsumer);
        otelOpentelemetryCollectorContainer = new GenericContainer("otel/opentelemetry-collector:0.33.0")
                .withExposedPorts(13133, 4317, 55680)
                .withNetwork(network)
                .withNetworkAliases("otel-collector")
                .withCommand("--config=/etc/otel-collector-config.yaml")
                .withClasspathResourceMapping("/given/otel-collector-config.yaml", "/etc/otel-collector-config.yaml", BindMode.READ_ONLY)
                .waitingFor(
                        Wait.forLogMessage(".*Everything is ready. Begin running and processing data.*\\n", 1));
        otelOpentelemetryCollectorContainer.start();
        otelOpentelemetryCollectorContainer.followOutput(logConsumer);
        return Map.of("quarkus.opentelemetry.tracer.exporter.otlp.endpoint", String.format("http://localhost:%d",
                otelOpentelemetryCollectorContainer.getMappedPort(55680)));
    }

    public static Integer getJaegerRestApiHostPort() {
        return jaegerTracingAllInOneContainer.getMappedPort(16686);
    }

    @Override
    public void stop() {
        if (jaegerTracingAllInOneContainer != null) {
            jaegerTracingAllInOneContainer.close();
        }
        if (otelOpentelemetryCollectorContainer != null) {
            otelOpentelemetryCollectorContainer.close();
        }
    }
}
