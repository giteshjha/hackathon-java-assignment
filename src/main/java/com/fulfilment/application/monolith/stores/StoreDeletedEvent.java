package com.fulfilment.application.monolith.stores;

public class StoreDeletedEvent {

  private final Store store;

  public StoreDeletedEvent(Store store) {
    this.store = store;
  }

  public Store getStore() {
    return store;
  }
}
