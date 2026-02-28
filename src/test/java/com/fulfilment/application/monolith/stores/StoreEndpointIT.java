package com.fulfilment.application.monolith.stores;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
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

  @Test
  void shouldAllowPatchingQuantityToZero() {
    // Regression test: PATCH must be able to set quantityProductsInStock to 0
    // (previously blocked by a wrong condition checking the *existing* value)
    String uniqueName = "StoreEndpointIT_Zero_" + System.nanoTime();

    Long id =
        given()
            .contentType("application/json")
            .body("{\"name\": \"" + uniqueName + "\", \"quantityProductsInStock\": 10}")
            .when()
            .post("/store")
            .then()
            .statusCode(201)
            .extract()
            .path("id");

    given()
        .contentType("application/json")
        .body("{\"name\": \"" + uniqueName + "\", \"quantityProductsInStock\": 0}")
        .when()
        .patch("/store/" + id)
        .then()
        .statusCode(200)
        .body("quantityProductsInStock", equalTo(0));

    given().when().delete("/store/" + id).then().statusCode(204);
  }

  @Test
  void shouldManageStoreProductMappingsAndKeepAggregateConsistent() {
    String uniqueStoreName = "StoreMappingIT_" + System.nanoTime();
    String uniqueProductName = "StoreMappingProduct_" + System.nanoTime();

    Long storeId =
        given()
            .contentType("application/json")
            .body("{\"name\": \"" + uniqueStoreName + "\", \"quantityProductsInStock\": 0}")
            .when()
            .post("/store")
            .then()
            .statusCode(201)
            .extract()
            .jsonPath()
            .getLong("id");

    Long productId =
        given()
            .contentType("application/json")
            .body(
                "{\"name\": \""
                    + uniqueProductName
                    + "\", \"description\": \"mapping\", \"price\": 9.99, \"stock\": 100}")
            .when()
            .post("/product")
            .then()
            .statusCode(201)
            .extract()
            .jsonPath()
            .getLong("id");

    given()
        .contentType("application/json")
        .body("{\"quantity\": 7}")
        .when()
        .put("/store/" + storeId + "/products/" + productId)
        .then()
        .statusCode(200)
        .body("quantity", equalTo(7));

    given()
        .when()
        .get("/store/" + storeId + "/products")
        .then()
        .statusCode(200)
        .body("size()", equalTo(1))
        .body("[0].productId", equalTo(productId.intValue()))
        .body("[0].quantity", equalTo(7));

    given()
        .when()
        .get("/store/" + storeId)
        .then()
        .statusCode(200)
        .body("quantityProductsInStock", equalTo(7));

    given()
        .contentType("application/json")
        .body("{\"quantity\": 3}")
        .when()
        .put("/store/" + storeId + "/products/" + productId)
        .then()
        .statusCode(200)
        .body("quantity", equalTo(3));

    given()
        .when()
        .get("/store/" + storeId)
        .then()
        .statusCode(200)
        .body("quantityProductsInStock", equalTo(3));

    given().when().delete("/store/" + storeId + "/products/" + productId).then().statusCode(204);

    given()
        .when()
        .get("/store/" + storeId)
        .then()
        .statusCode(200)
        .body("quantityProductsInStock", equalTo(0));

    given().when().delete("/store/" + storeId).then().statusCode(204);
    given().when().delete("/product/" + productId).then().statusCode(204);
  }
}
