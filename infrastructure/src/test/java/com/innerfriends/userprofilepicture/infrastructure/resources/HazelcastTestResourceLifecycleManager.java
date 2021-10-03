package com.innerfriends.userprofilepicture.infrastructure.resources;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.wait.strategy.Wait;

import java.util.Map;

public class HazelcastTestResourceLifecycleManager implements QuarkusTestResourceLifecycleManager {

    private final Logger logger = LoggerFactory.getLogger(HazelcastTestResourceLifecycleManager.class);

    private GenericContainer hazelcastContainer;

    @Override
    public Map<String, String> start() {
        final Slf4jLogConsumer logConsumer = new Slf4jLogConsumer(logger);
        hazelcastContainer = new GenericContainer("hazelcast/hazelcast:4.1.5")
                .withExposedPorts(5701)
                .waitingFor(
                        Wait.forLogMessage(".*is STARTED.*\\n", 1)
                );
        hazelcastContainer.start();
        hazelcastContainer.followOutput(logConsumer);

        return Map.of(
                "quarkus.hazelcast-client.cluster-name", "dev",
                "quarkus.hazelcast-client.cluster-members", String.format("localhost:%d", hazelcastContainer.getMappedPort(5701))
        );
    }

    @Override
    public void stop() {
        if (hazelcastContainer != null) {
            hazelcastContainer.close();
        }
    }
}
