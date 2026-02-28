package com.fulfilment.application.monolith.warehouses.adapters.restapi;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.hasSize;

import io.quarkus.test.junit.QuarkusTest;
import java.util.Map;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class WarehouseEndpointIT {

  @Test
  public void testSimpleListWarehouses() {
    given()
        .when()
        .get("warehouse")
        .then()
        .statusCode(200)
        .body(containsString("MWH.001"), containsString("MWH.012"), containsString("MWH.023"));
  }

  @Test
  public void testSearchFiltersByLocation() {
    given()
        .when()
        .get("warehouse/search?location=AMSTERDAM-001")
        .then()
        .statusCode(200)
        .body(containsString("MWH.012"), not(containsString("MWH.001")));
  }

  @Test
  public void testSearchExcludesArchivedWarehouses() {
    String code = "ARCH-SEARCH-" + System.currentTimeMillis();
    Map<String, Object> payload =
        Map.of(
            "businessUnitCode", code,
            "location", "AMSTERDAM-002",
            "capacity", 20,
            "stock", 5);

    given()
        .contentType("application/json")
        .body(payload)
        .when()
        .post("warehouse")
        .then()
        .statusCode(200);

    given().when().delete("warehouse/" + code).then().statusCode(204);

    given()
        .when()
        .get("warehouse/search?location=AMSTERDAM-002")
        .then()
        .statusCode(200)
        .body(not(containsString(code)));
  }

  @Test
  public void testSearchSortByCapacityWithPagination() {
    String lowCode = "PAG-LOW-" + System.currentTimeMillis();
    String highCode = "PAG-HIGH-" + System.currentTimeMillis();

    given()
        .contentType("application/json")
        .body(
            Map.of(
                "businessUnitCode", lowCode,
                "location", "EINDHOVEN-001",
                "capacity", 10,
                "stock", 2))
        .when()
        .post("warehouse")
        .then()
        .statusCode(200);

    given()
        .contentType("application/json")
        .body(
            Map.of(
                "businessUnitCode", highCode,
                "location", "EINDHOVEN-001",
                "capacity", 60,
                "stock", 3))
        .when()
        .post("warehouse")
        .then()
        .statusCode(200);

    given()
        .when()
        .get(
            "warehouse/search?location=EINDHOVEN-001&sortBy=capacity&sortOrder=desc&page=0&pageSize=1")
        .then()
        .statusCode(200)
        .body("$", hasSize(1))
        .body(containsString(highCode))
        .body(not(containsString(lowCode)));
  }

  @Test
  public void testSearchRejectsInvalidSortBy() {
    given()
        .when()
        .get("warehouse/search?sortBy=name")
        .then()
        .statusCode(400);
  }

  @Test
  public void testWarehouseProductMappingsKeepStockConsistent() {
    String code = "MAP-" + System.currentTimeMillis();
    String productName = "WarehouseMapProduct_" + System.nanoTime();

    given()
        .contentType("application/json")
        .body(
            Map.of(
                "businessUnitCode", code,
                "location", "EINDHOVEN-001",
                "capacity", 20,
                "stock", 0))
        .when()
        .post("warehouse")
        .then()
        .statusCode(200);

    Long productId =
        given()
            .contentType("application/json")
            .body(
                Map.of(
                    "name", productName,
                    "description", "warehouse mapping",
                    "price", 12.50,
                    "stock", 100))
            .when()
            .post("product")
            .then()
            .statusCode(201)
            .extract()
            .jsonPath()
            .getLong("id");

    given()
        .contentType("application/json")
        .body(Map.of("quantity", 12))
        .when()
        .put("warehouse/" + code + "/products/" + productId)
        .then()
        .statusCode(200)
        .body("quantity", org.hamcrest.Matchers.equalTo(12));

    given()
        .when()
        .get("warehouse/" + code + "/products")
        .then()
        .statusCode(200)
        .body("$", hasSize(1))
        .body("[0].productId", org.hamcrest.Matchers.equalTo(productId.intValue()))
        .body("[0].quantity", org.hamcrest.Matchers.equalTo(12));

    given().when().get("warehouse/" + code).then().statusCode(200).body("stock", org.hamcrest.Matchers.equalTo(12));

    given()
        .contentType("application/json")
        .body(Map.of("quantity", 25))
        .when()
        .put("warehouse/" + code + "/products/" + productId)
        .then()
        .statusCode(422);

    given().when().delete("warehouse/" + code + "/products/" + productId).then().statusCode(204);

    given().when().get("warehouse/" + code).then().statusCode(200).body("stock", org.hamcrest.Matchers.equalTo(0));

    given().when().delete("warehouse/" + code).then().statusCode(204);
    given().when().delete("product/" + productId).then().statusCode(204);
  }
}
