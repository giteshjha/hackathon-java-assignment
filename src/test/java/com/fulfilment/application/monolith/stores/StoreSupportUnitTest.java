package com.fulfilment.application.monolith.stores;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class StoreSupportUnitTest {

  @Test
  void legacyGatewayCanWriteForCreateAndUpdate() {
    LegacyStoreManagerGateway gateway = new LegacyStoreManagerGateway();
    Store store = new Store("LEGACY-STORE-UNIT");
    store.quantityProductsInStock = 5;

    assertDoesNotThrow(() -> gateway.createStoreOnLegacySystem(store));
    assertDoesNotThrow(() -> gateway.updateStoreOnLegacySystem(store));
  }
}
