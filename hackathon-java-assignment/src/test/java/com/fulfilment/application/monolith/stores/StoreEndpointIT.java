package com.fulfilment.application.monolith.stores;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

@QuarkusTest
class StoreEndpointIT {

  @Test
  void shouldListStores() {
    given().when().get("/store").then().statusCode(200).body("size()", greaterThanOrEqualTo(1));
  }

  @Test
  void shouldReturnValidationErrors() {
    given()
        .contentType("application/json")
        .body("{\"id\": 1, \"name\": \"INVALID\", \"quantityProductsInStock\": 1}")
        .when()
        .post("/store")
        .then()
        .statusCode(422);

    given()
        .contentType("application/json")
        .body("{\"name\": null, \"quantityProductsInStock\": 1}")
        .when()
        .put("/store/1")
        .then()
        .statusCode(422);
  }

  @Test
  void shouldCreateUpdatePatchAndDeleteStore() {
    String uniqueName = "StoreEndpointIT_" + System.nanoTime();

    Long id =
        given()
            .contentType("application/json")
            .body("{\"name\": \"" + uniqueName + "\", \"quantityProductsInStock\": 5}")
            .when()
            .post("/store")
            .then()
            .statusCode(201)
            .extract()
            .path("id");

    given().when().get("/store/" + id).then().statusCode(200);

    given()
        .contentType("application/json")
        .body("{\"name\": \"" + uniqueName + "_U\", \"quantityProductsInStock\": 7}")
        .when()
        .put("/store/" + id)
        .then()
        .statusCode(200);

    given()
        .contentType("application/json")
        .body("{\"name\": \"" + uniqueName + "_P\", \"quantityProductsInStock\": 9}")
        .when()
        .patch("/store/" + id)
        .then()
        .statusCode(200);

    given().when().delete("/store/" + id).then().statusCode(204);
  }
}
