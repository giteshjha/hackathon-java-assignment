package com.fulfilment.application.monolith.stores.domain.ports;

import com.fulfilment.application.monolith.stores.domain.models.Store;

/** Driven port: integration with the legacy store management system. */
public interface LegacyStoreGateway {

  void createStoreOnLegacySystem(Store store);

  void updateStoreOnLegacySystem(Store store);

  void deleteStoreOnLegacySystem(Store store);
}
