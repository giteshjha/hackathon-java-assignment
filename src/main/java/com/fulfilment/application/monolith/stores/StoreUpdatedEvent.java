package com.fulfilment.application.monolith.stores;

import com.fulfilment.application.monolith.stores.domain.models.Store;

public class StoreUpdatedEvent {
  private final Store store;

  public StoreUpdatedEvent(Store store) {
    this.store = store;
  }

  public Store getStore() {
    return store;
  }
}
