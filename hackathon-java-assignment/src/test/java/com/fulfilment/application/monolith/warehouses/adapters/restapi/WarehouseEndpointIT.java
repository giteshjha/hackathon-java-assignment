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
}
