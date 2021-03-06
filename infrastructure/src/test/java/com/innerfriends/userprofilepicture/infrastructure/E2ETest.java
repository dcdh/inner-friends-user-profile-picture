package com.innerfriends.userprofilepicture.infrastructure;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.hazelcast.core.HazelcastInstance;
import com.innerfriends.userprofilepicture.infrastructure.hazelcast.HazelcastCachedUserProfilePictures;
import com.innerfriends.userprofilepicture.infrastructure.hazelcast.HazelcastUserProfilePictureCacheRepository;
import com.innerfriends.userprofilepicture.infrastructure.resources.OpenTelemetryLifecycleManager;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ListObjectVersionsRequest;
import software.amazon.awssdk.services.s3.model.ObjectVersion;

import javax.inject.Inject;
import java.io.File;
import java.net.URL;
import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@Disabled// FIXME
@QuarkusTest
@TestMethodOrder(OrderAnnotation.class)
public class E2ETest {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static final class Traces {

        public List<Data> data;

        public List<String> getOperationNames() {
            return data
                    .stream()
                    .flatMap(d -> d.getSpans().stream())
                    .map(Span::getOperationName)
                    .collect(Collectors.toList());
        }

        public List<String> getOperationNamesInError() {
            return data
                    .stream()
                    .flatMap(d -> d.getSpans().stream())
                    .filter(Span::inError)
                    .map(Span::getOperationName)
                    .collect(Collectors.toList());
        }

        public List<Integer> getHttpStatus() {
            return data
                    .stream()
                    .flatMap(d -> d.getSpans().stream())
                    .flatMap(s -> s.httpStatus().stream())
                    .collect(Collectors.toList());
        }

    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static final class Data {

        public List<Span> spans;

        public List<Span> getSpans() {
            return spans;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static final class Span {

        public String operationName;
        public List<Tag> tags;

        public String getOperationName() {
            return operationName;
        }

        public boolean inError() {
            return tags.stream().anyMatch(Tag::inError);
        }

        public List<Integer> httpStatus() {
            return tags.stream()
                    .map(t -> t.httpStatus())
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        }

    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static final class Tag {

        public String key;
        public Object value;

        public boolean inError() {
            return "error".equals(key) && Boolean.TRUE.equals(value);
        }

        public Integer httpStatus() {
            if ("http.status_code".equals(key)) {
                return (Integer) value;
            }
            return null;
        }

    }

    @ConfigProperty(name = "bucket.user.profile.picture.name")
    String bucketUserProfilePictureName;

    @Inject
    S3Client s3Client;

    @Inject
    HazelcastInstance hazelcastInstance;

    @Test
    @Order(0)
    public void should_store_user_profile_picture() throws Exception {
        given()
                .multiPart("picture", getFileFromResource("given/1px_white.jpg"))
                .multiPart("supportedMediaType", "IMAGE_JPEG")
                .when()
                .post("/users/pseudoE2E/upload")
                .then()
                .log().all()
                .statusCode(201);

        final Integer hostPort = OpenTelemetryLifecycleManager.getJaegerRestApiHostPort();
        await().atMost(15, TimeUnit.SECONDS)
                .pollInterval(Duration.ofSeconds(1l))
                .until(() -> {
                    final Traces traces = given()
                            .when()
                            .queryParam("limit", "1")
                            .queryParam("service", "user-profile-picture")
                            .queryParam("tags", "{\"http.target\":\"/users/pseudoE2E/upload\"}")
                            .get(new URL(String.format("http://localhost:%d/api/traces", hostPort)))
                            .then()
                            .log().all()
                            .contentType(ContentType.JSON)
                            .extract()
                            .body().as(Traces.class);
                    if (traces.getOperationNames().isEmpty()) {
                        return false;
                    }
                    return traces.getOperationNames().containsAll(List.of("users/{userPseudo}/upload", "S3ProfilePictureRepository.save",
                            "HazelcastUserProfilePictureCacheRepository.store"))
                            && traces.getHttpStatus().containsAll(List.of(201))
                            && traces.getOperationNamesInError().isEmpty();
        });

        final List<ObjectVersion> objectVersions = s3Client.listObjectVersions(ListObjectVersionsRequest
                .builder()
                .bucket(bucketUserProfilePictureName)
                .prefix("pseudoE2E.jpeg")
                .build()).versions();
        assertThat(objectVersions.size()).isEqualTo(1);

        assertThat(hazelcastInstance.getMap(HazelcastUserProfilePictureCacheRepository.MAP_NAME).get("pseudoE2E")).isNotNull();
        assertThat(((HazelcastCachedUserProfilePictures) hazelcastInstance.getMap(HazelcastUserProfilePictureCacheRepository.MAP_NAME).get("pseudoE2E"))
                .featuredUserProfilePictureIdentifier).isNotNull();
    }

    @Test
    @Order(1)
    public void should_get_featured_user_profile_picture() {
        given()
                .header("Content-Type", "image/jpeg")
                .when()
                .get("/users/pseudoE2E/featured")
                .then()
                .statusCode(200);

        final Integer hostPort = OpenTelemetryLifecycleManager.getJaegerRestApiHostPort();
        await().atMost(15, TimeUnit.SECONDS)
                .pollInterval(Duration.ofSeconds(1l))
                .until(() -> {
                    final Traces traces = given()
                            .when()
                            .queryParam("limit", "1")
                            .queryParam("service", "user-profile-picture")
                            .queryParam("tags", "{\"http.target\":\"/users/pseudoE2E/featured\"}")
                            .get(new URL(String.format("http://localhost:%d/api/traces", hostPort)))
                            .then()
                            .log().all()
                            .contentType(ContentType.JSON)
                            .extract()
                            .body().as(Traces.class);
                    if (traces.getOperationNames().isEmpty()) {
                        return false;
                    }
                    return traces.getOperationNames().containsAll(List.of("users/{userPseudo}/featured", "S3ProfilePictureRepository.getLast",
                            "HazelcastUserProfilePictureCacheRepository.get"))
                            && traces.getHttpStatus().containsAll(List.of(200))
                            && traces.getOperationNamesInError().isEmpty();
        });
    }

    @Test
    @Order(2)
    public void should_get_featured_user_profile_picture_return_404_when_picture_does_not_exist() {
        given()
                .header("Content-Type", "image/jpeg")
                .when()
                .get("/users/unknownPseudoE2E/featured")
                .then()
                .statusCode(404);

        final Integer hostPort = OpenTelemetryLifecycleManager.getJaegerRestApiHostPort();
        await().atMost(10, TimeUnit.SECONDS)
                .pollInterval(Duration.ofSeconds(1l))
                .until(() -> {
                    final Traces traces = given()
                            .when()
                            .queryParam("limit", "1")
                            .queryParam("service", "user-profile-picture")
                            .queryParam("tags", "{\"http.target\":\"/users/unknownPseudoE2E/featured\"}")
                            .get(new URL(String.format("http://localhost:%d/api/traces", hostPort)))
                            .then()
                            .log().all()
                            .contentType(ContentType.JSON)
                            .extract()
                            .body().as(Traces.class);
                    if (traces.getOperationNames().isEmpty()) {
                        return false;
                    }
                    return traces.getOperationNames().containsAll(List.of("users/{userPseudo}/featured", "S3ProfilePictureRepository.getLast",
                            "HazelcastUserProfilePictureCacheRepository.get"))
                            && traces.getHttpStatus().containsAll(List.of(404))
                            && traces.getOperationNamesInError().isEmpty();
        });

        final List<ObjectVersion> objectVersions = s3Client.listObjectVersions(ListObjectVersionsRequest
                .builder()
                .bucket(bucketUserProfilePictureName)
                .prefix("unknownPseudoE2E.jpeg")
                .build()).versions();
        assertThat(objectVersions.isEmpty()).isTrue();

        assertThat(hazelcastInstance.getMap(HazelcastUserProfilePictureCacheRepository.MAP_NAME).get("unknownPseudoE2E")).isNull();
    }

    @Test
    @Order(3)
    public void should_list_user_profile_pictures() {
        given()
                .header("Content-Type", "image/jpeg")
                .when()
                .get("/users/pseudoE2E")
                .then()
                .statusCode(200);

        final Integer hostPort = OpenTelemetryLifecycleManager.getJaegerRestApiHostPort();
        await().atMost(15, TimeUnit.SECONDS)
                .pollInterval(Duration.ofSeconds(1l))
                .until(() -> {
                    final Traces traces = given()
                            .when()
                            .queryParam("limit", "1")
                            .queryParam("service", "user-profile-picture")
                            .queryParam("tags", "{\"http.target\":\"/users/pseudoE2E\"}")
                            .get(new URL(String.format("http://localhost:%d/api/traces", hostPort)))
                            .then()
                            .log().all()
                            .contentType(ContentType.JSON)
                            .extract()
                            .body().as(Traces.class);
                    if (traces.getOperationNames().isEmpty()) {
                        return false;
                    }
                    return traces.getOperationNames().containsAll(List.of("users/{userPseudo}", "S3ProfilePictureRepository.listByUserPseudo",
                            "HazelcastUserProfilePictureCacheRepository.get"))
                            && traces.getHttpStatus().containsAll(List.of(200))
                            && traces.getOperationNamesInError().isEmpty();
                });

        assertThat(hazelcastInstance.getMap(HazelcastUserProfilePictureCacheRepository.MAP_NAME).get("pseudoE2E")).isNotNull();
        assertThat(((HazelcastCachedUserProfilePictures) hazelcastInstance.getMap(HazelcastUserProfilePictureCacheRepository.MAP_NAME).get("pseudoE2E"))
                .userProfilePictureIdentifiers).isNotNull();
    }

    @Test
    @Order(4)
    public void should_download_user_profile_picture_by_version_id() {
        final String versionId = s3Client.listObjectVersions(ListObjectVersionsRequest
                .builder()
                .bucket(bucketUserProfilePictureName)
                .prefix("pseudoE2E.jpeg")
                .build()).versions().stream().map(ObjectVersion::versionId)
                .findFirst().get();
        given()
                .header("Content-Type", "image/jpeg")
                .when()
                .get("/users/pseudoE2E/version/{versionId}", versionId)
                .then()
                .statusCode(200);

        final Integer hostPort = OpenTelemetryLifecycleManager.getJaegerRestApiHostPort();
        await().atMost(15, TimeUnit.SECONDS)
                .pollInterval(Duration.ofSeconds(1l))
                .until(() -> {
                    final Traces traces = given()
                            .when()
                            .queryParam("limit", "1")
                            .queryParam("service", "user-profile-picture")
                            .queryParam("tags", String.format("{\"http.target\":\"/users/pseudoE2E/version/%s\"}", versionId))
                            .get(new URL(String.format("http://localhost:%d/api/traces", hostPort)))
                            .then()
                            .log().all()
                            .contentType(ContentType.JSON)
                            .extract()
                            .body().as(Traces.class);
                    if (traces.getOperationNames().isEmpty()) {
                        return false;
                    }
                    return traces.getOperationNames().containsAll(List.of("users/{userPseudo}/version/{versionId}", "S3ProfilePictureRepository.getContentByVersion"))
                            && traces.getHttpStatus().containsAll(List.of(200))
                            && traces.getOperationNamesInError().isEmpty();
                });
    }

    private File getFileFromResource(final String fileName) throws Exception {
        final ClassLoader classLoader = getClass().getClassLoader();
        final URL resource = classLoader.getResource(fileName);
        return new File(resource.toURI());
    }

}
