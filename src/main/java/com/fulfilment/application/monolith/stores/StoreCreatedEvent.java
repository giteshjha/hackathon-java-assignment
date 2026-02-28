package com.fulfilment.application.monolith.stores;

import com.fulfilment.application.monolith.stores.domain.models.Store;

public class StoreCreatedEvent {
  private final Store store;

  public StoreCreatedEvent(Store store) {
    this.store = store;
  }

  public Store getStore() {
    return store;
  }
}
