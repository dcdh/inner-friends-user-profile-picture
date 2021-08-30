package com.innerfriends.userprofilepicture.infrastructure.resources;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.wait.strategy.Wait;

import java.util.Collections;
import java.util.Map;

// https://www.zenko.io/blog/deploy-zenko-1-1-on-bare-metal-private-or-public-cloud/
public class ZenkoTestResourceLifecycleManager implements QuarkusTestResourceLifecycleManager {

    private final Logger logger = LoggerFactory.getLogger(ZenkoTestResourceLifecycleManager.class);

    private GenericContainer zenkoContainer;

    @Override
    public Map<String, String> start() {
        final Slf4jLogConsumer logConsumer = new Slf4jLogConsumer(logger);
        zenkoContainer = new GenericContainer("zenko/cloudserver:8.2.7")
                .withExposedPorts(8000)
                .withEnv("SCALITY_ACCESS_KEY_ID", "test-key")
                .withEnv("SCALITY_SECRET_ACCESS_KEY", "test-secret")
                .withEnv("S3BACKEND", "mem")
                .waitingFor(
                        Wait.forLogMessage(".*server started.*\\n", 1)
                );
        zenkoContainer.start();
        zenkoContainer.followOutput(logConsumer);
        System.setProperty("quarkus.s3.endpoint-override", String.format("http://localhost:%d", zenkoContainer.getMappedPort(8000)));
        return Collections.emptyMap();
    }

    @Override
    public void stop() {
        System.clearProperty("quarkus.s3.endpoint-override");
        if (zenkoContainer != null) {
            zenkoContainer.close();
        }
    }
}
