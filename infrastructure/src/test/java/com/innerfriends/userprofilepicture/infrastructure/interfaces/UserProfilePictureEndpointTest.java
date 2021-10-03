package com.innerfriends.userprofilepicture.infrastructure.interfaces;

import com.innerfriends.userprofilepicture.domain.*;
import com.innerfriends.userprofilepicture.domain.usecase.GetUserUserProfilePictureByVersionCommand;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import io.restassured.module.jsv.JsonSchemaValidator;
import io.smallrye.mutiny.Uni;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.util.Collections;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.*;

@QuarkusTest
public class UserProfilePictureEndpointTest {

    @InjectMock
    private UserProfilePictureRepository userProfilePictureRepository;

    @Test
    public void should_upload_user_profile_picture() throws Exception {
        // Given
        doReturn(Uni.createFrom().item(new TestUserProfilePictureSaved())).when(userProfilePictureRepository).save(
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
                .body("mediaType", equalTo("IMAGE_JPEG"))
                .body("versionId", equalTo("v0"))
                .body("featured", equalTo(false));
        verify(userProfilePictureRepository, times(1)).save(any(), any(), any());
    }

    @Test
    public void should_upload_user_profile_picture_return_expected_response_when_profile_picture_repository_exception_is_thrown() throws Exception {
        // Given
        doReturn(Uni.createFrom().failure(new UserProfilePictureRepositoryException())).when(userProfilePictureRepository).save(
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
        verify(userProfilePictureRepository, times(1)).save(any(), any(), any());
    }

    @Test
    public void should_get_featured_user_profile_picture() {
        // Given
        doReturn(Uni.createFrom().item(new TestContentUserProfilePicture())).when(userProfilePictureRepository)
                .getLast(new JaxRsUserPseudo("pseudo"), SupportedMediaType.IMAGE_JPEG);

        // When && Then
        given()
                .header("Content-Type", "image/jpeg")
                .when()
                .get("/users/pseudo/featured")
                .then()
                .log().headers()
                .statusCode(200)
                .body(JsonSchemaValidator.matchesJsonSchemaInClasspath("expected/profilePictureIdentifier.json"))
                .body("userPseudo", equalTo("pseudo"))
                .body("mediaType", equalTo("IMAGE_JPEG"))
                .body("versionId", equalTo("v0"));
        verify(userProfilePictureRepository, times(1)).getLast(any(), any());
    }

    @Test
    public void should_get_featured_user_profile_picture_return_expected_response_when_profile_picture_not_available_yet_is_thrown() {
        // Given
        doReturn(Uni.createFrom().failure(new UserProfilePictureNotAvailableYetException(new JaxRsUserPseudo("pseudo"))))
                .when(userProfilePictureRepository).getLast(new JaxRsUserPseudo("pseudo"), SupportedMediaType.IMAGE_JPEG);

        // When && Then
        given()
                .header("Content-Type", "image/jpeg")
                .when()
                .get("/users/pseudo/featured")
                .then()
                .log().all()
                .statusCode(404);
        verify(userProfilePictureRepository, times(1)).getLast(any(), any());
    }

    @Test
    public void should_get_featured_user_profile_picture_return_expected_response_when_profile_picture_repository_exception_is_thrown() {
        // Given
        doReturn(Uni.createFrom().failure(new UserProfilePictureRepositoryException()))
                .when(userProfilePictureRepository).getLast(new JaxRsUserPseudo("pseudo"), SupportedMediaType.IMAGE_JPEG);

        // When && Then
        given()
                .header("Content-Type", "image/jpeg")
                .when()
                .get("/users/pseudo/featured")
                .then()
                .log().all()
                .statusCode(500);
        verify(userProfilePictureRepository, times(1)).getLast(any(), any());
    }

    @Test
    public void should_list_user_profile_pictures() {
        // Given
        doReturn(Uni.createFrom().item(Collections.singletonList(new TestUserProfilePictureIdentifier())))
                .when(userProfilePictureRepository)
                .listByUserPseudo(new JaxRsUserPseudo("pseudo"), SupportedMediaType.IMAGE_JPEG);

        // When && Then
        given()
                .header("Content-Type", "image/jpeg")
                .when()
                .get("/users/pseudo")
                .then()
                .log().all()
                .statusCode(200)
                .body(JsonSchemaValidator.matchesJsonSchemaInClasspath("expected/profilePictures.json"))
                .body("featureState", equalTo("NOT_SELECTED_YET"))
                .body("userProfilePictures[0].userPseudo", equalTo("pseudo"))
                .body("userProfilePictures[0].mediaType", equalTo("IMAGE_JPEG"))
                .body("userProfilePictures[0].versionId", equalTo("v0"))
                .body("userProfilePictures[0].featured", equalTo(true));

        verify(userProfilePictureRepository, times(1)).listByUserPseudo(any(), any());
    }

    @Test
    public void should_list_user_profile_pictures_return_expected_response_when_profile_picture_repository_exception_is_thrown() {
        // Given
        doReturn(Uni.createFrom().failure(new UserProfilePictureRepositoryException()))
                .when(userProfilePictureRepository)
                .listByUserPseudo(new JaxRsUserPseudo("pseudo"), SupportedMediaType.IMAGE_JPEG);

        // When && Then
        given()
                .header("Content-Type", "image/jpeg")
                .when()
                .get("/users/pseudo")
                .then()
                .log().all()
                .statusCode(500);
        verify(userProfilePictureRepository, times(1)).listByUserPseudo(any(), any());
    }

    @Test
    public void should_download_user_profile_picture_by_version() {
        // Given
        doReturn(Uni.createFrom().item(new TestContentUserProfilePicture())).when(userProfilePictureRepository)
                .getContentByVersionId(new GetUserUserProfilePictureByVersionCommand(
                        new JaxRsUserPseudo("pseudo"),
                        SupportedMediaType.IMAGE_JPEG,
                        new JaxRsVersionId("v0")));

        // When && Then
        given()
                .header("Content-Type", "image/jpeg")
                .when()
                .get("/users/pseudo/version/v0")
                .then()
                .log().headers()
                .statusCode(200)
                .header("Content-Disposition", "attachment;filename=pseudo.jpeg")
                .header("Content-Type","image/jpeg")
                .header("Content-Length","7")
                .header("versionId","v0");
        verify(userProfilePictureRepository, times(1)).getContentByVersionId(any());
    }

    @Test
    public void should_download_user_profile_picture_by_version_return_expected_response_when_profile_picture_version_unknown_is_thrown() {
        // Given
        doReturn(Uni.createFrom().failure(new UserProfilePictureVersionUnknownException(mock(UserProfilePictureIdentifier.class))))
                .when(userProfilePictureRepository).getContentByVersionId(new GetUserUserProfilePictureByVersionCommand(
                new JaxRsUserPseudo("pseudo"),
                SupportedMediaType.IMAGE_JPEG,
                new JaxRsVersionId("v0")));

        // When && Then
        given()
                .header("Content-Type", "image/jpeg")
                .when()
                .get("/users/pseudo/version/v0")
                .then()
                .log().all()
                .statusCode(404);
        verify(userProfilePictureRepository, times(1)).getContentByVersionId(any());
    }

    @Test
    public void should_download_user_profile_picture_by_version_return_expected_response_when_profile_picture_repository_exception_is_thrown() {
        // Given
        doReturn(Uni.createFrom().failure(new UserProfilePictureRepositoryException())).when(userProfilePictureRepository)
                .getContentByVersionId(new GetUserUserProfilePictureByVersionCommand(
                        new JaxRsUserPseudo("pseudo"),
                        SupportedMediaType.IMAGE_JPEG,
                        new JaxRsVersionId("v0")));

        // When && Then
        given()
                .header("Content-Type", "image/jpeg")
                .when()
                .get("/users/pseudo/version/v0")
                .then()
                .log().all()
                .statusCode(500);
        verify(userProfilePictureRepository, times(1)).getContentByVersionId(any());
    }

    private File getFileFromResource(final String fileName) throws Exception {
        final ClassLoader classLoader = getClass().getClassLoader();
        final URL resource = classLoader.getResource(fileName);
        return new File(resource.toURI());
    }

}
