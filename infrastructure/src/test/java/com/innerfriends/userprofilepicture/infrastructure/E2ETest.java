package com.innerfriends.userprofilepicture.infrastructure;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.io.File;
import java.net.URL;

import static io.restassured.RestAssured.given;

@QuarkusTest
@TestMethodOrder(OrderAnnotation.class)
public class E2ETest {

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
    }

    @Test
    @Order(1)
    public void should_get_last_user_profile_picture() {
        given()
                .when()
                .get("/users/pseudoE2E")
                .then()
                .statusCode(200);
    }

    @Test
    @Order(2)
    public void should_get_last_user_profile_picture_return_404_when_picture_does_not_exist() {
        given()
                .when()
                .get("/users/unknownPseudoE2E")
                .then()
                .statusCode(404);
    }

    private File getFileFromResource(final String fileName) throws Exception {
        final ClassLoader classLoader = getClass().getClassLoader();
        final URL resource = classLoader.getResource(fileName);
        return new File(resource.toURI());
    }

}
