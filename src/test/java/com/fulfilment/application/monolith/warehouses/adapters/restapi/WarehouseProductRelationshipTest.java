package com.fulfilment.application.monolith.warehouses.adapters.restapi;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

import io.quarkus.test.junit.QuarkusTest;
import java.util.Map;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class WarehouseProductRelationshipTest {

  @Test
  public void testWarehouseProductMappingCrudValidationAndConsistency() {
    String code = "REL-" + System.currentTimeMillis();
    String productName = "WarehouseRelProduct_" + System.nanoTime();

    given()
        .contentType("application/json")
        .body(Map.of("businessUnitCode", code, "location", "EINDHOVEN-001", "capacity", 15, "stock", 0))
        .when()
        .post("/warehouse")
        .then()
        .statusCode(200);

    Long productId =
        given()
            .contentType("application/json")
            .body(Map.of("name", productName, "description", "rel", "price", 10.25, "stock", 100))
            .when()
            .post("/product")
            .then()
            .statusCode(201)
            .extract()
            .jsonPath()
            .getLong("id");

    given()
        .contentType("application/json")
        .body(Map.of("quantity", 9))
        .when()
        .put("/warehouse/" + code + "/products/" + productId)
        .then()
        .statusCode(200)
        .body("quantity", equalTo(9));

    given()
        .when()
        .get("/warehouse/" + code + "/products")
        .then()
        .statusCode(200)
        .body("size()", equalTo(1))
        .body("[0].productId", equalTo(productId.intValue()))
        .body("[0].quantity", equalTo(9));

    given().when().get("/warehouse/" + code).then().statusCode(200).body("stock", equalTo(9));

    given()
        .contentType("application/json")
        .body(Map.of("quantity", 16))
        .when()
        .put("/warehouse/" + code + "/products/" + productId)
        .then()
        .statusCode(422);

    given().when().delete("/warehouse/" + code + "/products/" + productId).then().statusCode(204);
    given().when().delete("/warehouse/" + code + "/products/" + productId).then().statusCode(404);

    // Lenient conversion for "null" query values must not fail.
    given()
        .when()
        .get(
            "/warehouse/search?location=EINDHOVEN-001&minCapacity=null&maxCapacity=null&sortBy=createdAt&sortOrder=asc&page=0&pageSize=10")
        .then()
        .statusCode(200);

    // Archived warehouse mappings are read-only.
    given().when().delete("/warehouse/" + code).then().statusCode(204);
    given()
        .contentType("application/json")
        .body(Map.of("quantity", 2))
        .when()
        .put("/warehouse/" + code + "/products/" + productId)
        .then()
        .statusCode(409);

    given().when().delete("/product/" + productId).then().statusCode(204);
  }
}
