package com.innerfriends.userprofilepicture.infrastructure.resources;

import io.quarkus.test.common.QuarkusTestResource;

@QuarkusTestResource(OpenTelemetryLifecycleManager.class)
@QuarkusTestResource(ZenkoTestResourceLifecycleManager.class)
public class QuarkusTestResources {
}
