package com.fulfilment.application.monolith.health;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

@QuarkusTest
class HealthEndpointIT {

  @Test
  void healthEndpointShouldReturnUp() {
    given()
        .when()
        .get("/q/health")
        .then()
        .statusCode(200)
        .body("status", equalTo("UP"));
  }
}
