package com.fulfilment.application.monolith.stores;

import static io.restassured.RestAssured.given;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.fulfilment.application.monolith.stores.domain.models.Store;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/**
 * Integration test for store event handling and transaction integrity.
 *
 * Verifies that the legacy system is only notified when store
 * operations complete successfully.
 */
@QuarkusTest
public class StoreTransactionIntegrationTest {

  @InjectMock
  LegacyStoreManagerGateway legacyGateway;

  @Test
  public void testLegacySystemNotNotifiedOnFailedStoreCreation() throws InterruptedException {
    Mockito.reset(legacyGateway);

    String uniqueName = "IntegrationTest_" + System.currentTimeMillis();

    // First create should succeed
    given()
        .contentType("application/json")
        .body("{\"name\": \"" + uniqueName + "\", \"quantityProductsInStock\": 5}")
        .when().post("/store")
        .then()
        .statusCode(201);

    Thread.sleep(1000);

    verify(legacyGateway, times(1)).createStoreOnLegacySystem(any(Store.class));

    Mockito.reset(legacyGateway);

    // Second create with same name should fail (unique constraint violation)
    given()
        .contentType("application/json")
        .body("{\"name\": \"" + uniqueName + "\", \"quantityProductsInStock\": 10}")
        .when().post("/store")
        .then()
        .statusCode(500);

    Thread.sleep(1000);

    verify(legacyGateway, never()).createStoreOnLegacySystem(any(Store.class));
  }

  @Test
  public void testLegacySystemNotifiedOnDelete() throws InterruptedException {
    Mockito.reset(legacyGateway);

    String uniqueName = "IntegrationTest_Delete_" + System.currentTimeMillis();

    Long id =
        given()
            .contentType("application/json")
            .body("{\"name\": \"" + uniqueName + "\", \"quantityProductsInStock\": 3}")
            .when().post("/store")
            .then()
            .statusCode(201)
            .extract()
            .jsonPath().getLong("id");

    Thread.sleep(500);
    Mockito.reset(legacyGateway);

    given().when().delete("/store/" + id).then().statusCode(204);

    Thread.sleep(1000);

    verify(legacyGateway, times(1)).deleteStoreOnLegacySystem(any(Store.class));
  }
}
