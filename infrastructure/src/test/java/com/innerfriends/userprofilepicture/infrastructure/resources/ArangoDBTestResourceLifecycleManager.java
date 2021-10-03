package com.innerfriends.userprofilepicture.infrastructure.resources;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.wait.strategy.Wait;

import java.util.Map;

public class ArangoDBTestResourceLifecycleManager implements QuarkusTestResourceLifecycleManager {

    private final Logger logger = LoggerFactory.getLogger(ArangoDBTestResourceLifecycleManager.class);

    private GenericContainer<?> arangodbContainer;

    @Override
    public Map<String, String> start() {
        final Slf4jLogConsumer logConsumer = new Slf4jLogConsumer(logger);
        arangodbContainer = new GenericContainer<>("arangodb:3.7.11")
                .withExposedPorts(8529)
                .withNetworkAliases("arangodb")
                .withEnv("ARANGO_ROOT_PASSWORD", "password")
                .withEnv("ARANGODB_OVERRIDE_DETECTED_TOTAL_MEMORY", "100m")
                .waitingFor(Wait.forLogMessage(".*is ready for business.*", 1));
        arangodbContainer.start();
        arangodbContainer.followOutput(logConsumer);

        return Map.of(
                "arangodb.host", "localhost",
                "arangodb.port", arangodbContainer.getMappedPort(8529).toString(),
                "arangodb.user", "root",
                "arangodb.password", "password",
                "arangodb.dbName", "friends"
        );
    }

    @Override
    public void stop() {
        if (arangodbContainer != null) {
            arangodbContainer.close();
        }
    }

}
