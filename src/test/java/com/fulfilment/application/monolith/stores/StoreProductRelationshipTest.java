package com.fulfilment.application.monolith.stores;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class StoreProductRelationshipTest {

  @Test
  public void testStoreProductMappingCrudAndValidation() {
    String storeName = "StoreRel_" + System.nanoTime();
    String productName = "StoreRelProduct_" + System.nanoTime();

    Long storeId =
        given()
            .contentType("application/json")
            .body("{\"name\":\"" + storeName + "\",\"quantityProductsInStock\":0}")
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
                "{\"name\":\""
                    + productName
                    + "\",\"description\":\"rel\",\"price\":5.50,\"stock\":20}")
            .when()
            .post("/product")
            .then()
            .statusCode(201)
            .extract()
            .jsonPath()
            .getLong("id");

    given()
        .contentType("application/json")
        .body("{\"quantity\":2}")
        .when()
        .put("/store/" + storeId + "/products/" + productId)
        .then()
        .statusCode(200)
        .body("quantity", equalTo(2));

    given()
        .when()
        .get("/store/" + storeId + "/products")
        .then()
        .statusCode(200)
        .body("size()", equalTo(1))
        .body("[0].productId", equalTo(productId.intValue()))
        .body("[0].quantity", equalTo(2));

    given().when().get("/store/" + storeId).then().statusCode(200).body("quantityProductsInStock", equalTo(2));

    given()
        .contentType("application/json")
        .body("{\"quantity\":0}")
        .when()
        .put("/store/" + storeId + "/products/" + productId)
        .then()
        .statusCode(422);

    given().when().delete("/store/" + storeId + "/products/" + productId).then().statusCode(204);
    given().when().delete("/store/" + storeId + "/products/" + productId).then().statusCode(404);

    given().when().delete("/store/" + storeId).then().statusCode(204);
    given().when().delete("/product/" + productId).then().statusCode(204);
  }
}
