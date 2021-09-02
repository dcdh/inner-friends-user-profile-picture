## What

Store user profile picture into a s3 server like real amazon S3 service or Zenko.

## Prerequisites

`docker` is mandatory to be able to build the project. The infrastructure relie on `testcontainers` to start containers before starting tests and kill and remove them after running the tests.

## How to build and run

1. run to prepare application containers `docker pull zenko/cloudserver:8.2.7 && docker pull jaegertracing/all-in-one:1.25.0 && docker pull otel/opentelemetry-collector:0.33.0`
1. run `mvn clean install verify` to build everything;
1. run `docker build -f infrastructure/src/main/docker/Dockerfile.native-distroless -t damdamdeo/inner-friends-user-profile-picture infrastructure/` to build docker image
1. run `docker-compose -f docker-compose-local-run.yaml up` to start the stack
1. access swagger ui via `http://0.0.0.0:8080/q/swagger-ui/`
1. access jaeger ui via `http://localhost:16686/`

## Infra

### Zenko

`ENDPOINT` need to be defined however the `inner-friends-user-profile-picture` application will not be able to connect to zenko having this error **Connection refused**