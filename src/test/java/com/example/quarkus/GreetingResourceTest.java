package com.example.quarkus;

import static org.hamcrest.Matchers.containsString;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import static io.restassured.RestAssured.given;

@QuarkusTest
public class GreetingResourceTest {

    @Test
    public void testHelloEndpoint() {
        given()
                .when().get("/api/hello")
                .then()
                .statusCode(200)
                .body(containsString("Hello, World!"));
    }

    @Test
    public void testHelloWithNameEndpoint() {
        given()
                .when().get("/api/hello?name=Quarkus")
                .then()
                .statusCode(200)
                .body(containsString("Hello, Quarkus!"));
    }

    @Test
    public void testStatusEndpoint() {
        given()
                .when().get("/api/status")
                .then()
                .statusCode(200)
                .body(containsString("UP"));
    }

    @Test
    public void testInfoEndpoint() {
        given()
                .when().get("/api/info")
                .then()
                .statusCode(200)
                .body(containsString("Simple Quarkus Application"));
    }
}
