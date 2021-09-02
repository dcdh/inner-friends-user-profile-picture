package com.innerfriends.userprofilepicture.infrastructure.interfaces;

import com.innerfriends.userprofilepicture.domain.*;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import io.restassured.module.jsv.JsonSchemaValidator;
import io.smallrye.mutiny.Uni;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.URL;
import java.nio.file.Files;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.*;

@QuarkusTest
public class UserProfilePictureEndpointTest {

    @InjectMock
    private ProfilePictureRepository profilePictureRepository;

    private static final class TestProfilePicture implements ProfilePicture {

        @Override
        public UserPseudo userPseudo() {
            return () -> "pseudo";
        }

        @Override
        public byte[] picture() {
            return "picture".getBytes();
        }

        @Override
        public SupportedMediaType mediaType() {
            return SupportedMediaType.IMAGE_JPEG;
        }

        @Override
        public Long contentLength() {
            return 7L;
        }

        @Override
        public String versionId() {
            return "v0";
        }
    }

    private static final class TestProfilePictureSaved implements ProfilePictureSaved {

        @Override
        public UserPseudo userPseudo() {
            return () -> "pseudo";
        }

        @Override
        public String versionId() {
            return "v0";
        }
    }

    @Test
    public void should_upload_user_profile_picture() throws Exception {
        // Given
        doReturn(Uni.createFrom().item(new TestProfilePictureSaved())).when(profilePictureRepository).save(
                new JaxRsUserPseudo("pseudo"),
                Files.readAllBytes(getFileFromResource("given/1px_white.jpg").toPath()),
                SupportedMediaType.IMAGE_JPEG);

        // When && Then
        given()
                .multiPart("picture", getFileFromResource("given/1px_white.jpg"))
                .multiPart("supportedMediaType", "IMAGE_JPEG")
                .when()
                .post("/users/pseudo/upload")
                .then()
                .log().all()
                .statusCode(201)
                .body(JsonSchemaValidator.matchesJsonSchemaInClasspath("expected/profilePictureSaved.json"))
                .body("userPseudo", equalTo("pseudo"))
                .body("versionId", equalTo("v0"));
        verify(profilePictureRepository, times(1)).save(any(), any(), any());
    }

    @Test
    public void should_upload_user_profile_picture_return_expected_response_when_profile_picture_repository_exception_is_thrown() throws Exception {
        // Given
        doReturn(Uni.createFrom().failure(new ProfilePictureRepositoryException())).when(profilePictureRepository).save(
                new JaxRsUserPseudo("pseudo"),
                Files.readAllBytes(getFileFromResource("given/1px_white.jpg").toPath()),
                SupportedMediaType.IMAGE_JPEG);

        // When && Then
        given()
                .multiPart("picture", getFileFromResource("given/1px_white.jpg"))
                .multiPart("supportedMediaType", "IMAGE_JPEG")
                .when()
                .post("/users/pseudo/upload")
                .then()
                .log().all()
                .statusCode(500);
        verify(profilePictureRepository, times(1)).save(any(), any(), any());
    }

    @Test
    public void should_get_last_user_profile_picture() {
        // Given
        doReturn(Uni.createFrom().item(new TestProfilePicture())).when(profilePictureRepository)
                .getLast(new JaxRsUserPseudo("pseudo"), SupportedMediaType.IMAGE_JPEG);

        // When && Then
        given()
                .header("Content-Type", "image/jpeg")
                .when()
                .get("/users/pseudo")
                .then()
                .log().headers()
                .statusCode(200)
                .header("Content-Disposition", "attachment;filename=pseudo.jpeg")
                .header("Content-Type","image/jpeg")
                .header("Content-Length","7")
                .header("versionId","v0");
        verify(profilePictureRepository, times(1)).getLast(any(), any());
    }

    @Test
    public void should_get_last_user_profile_picture_return_expected_response_when_profile_picture_not_available_yet_is_thrown() {
        // Given
        doReturn(Uni.createFrom().failure(new ProfilePictureNotAvailableYetException(new JaxRsUserPseudo("pseudo"))))
                .when(profilePictureRepository).getLast(new JaxRsUserPseudo("pseudo"), SupportedMediaType.IMAGE_JPEG);

        // When && Then
        given()
                .header("Content-Type", "image/jpeg")
                .when()
                .get("/users/pseudo")
                .then()
                .log().all()
                .statusCode(404);
        verify(profilePictureRepository, times(1)).getLast(any(), any());
    }

    @Test
    public void should_get_last_user_profile_picture_return_expected_response_when_profile_picture_repository_exception_is_thrown() {
        // Given
        doReturn(Uni.createFrom().failure(new ProfilePictureRepositoryException()))
                .when(profilePictureRepository).getLast(new JaxRsUserPseudo("pseudo"), SupportedMediaType.IMAGE_JPEG);

        // When && Then
        given()
                .header("Content-Type", "image/jpeg")
                .when()
                .get("/users/pseudo")
                .then()
                .log().all()
                .statusCode(500);
        verify(profilePictureRepository, times(1)).getLast(any(), any());
    }

    private File getFileFromResource(final String fileName) throws Exception {
        final ClassLoader classLoader = getClass().getClassLoader();
        final URL resource = classLoader.getResource(fileName);
        return new File(resource.toURI());
    }

}
