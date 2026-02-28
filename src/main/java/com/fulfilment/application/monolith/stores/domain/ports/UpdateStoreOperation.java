package com.fulfilment.application.monolith.stores.domain.ports;

import com.fulfilment.application.monolith.stores.domain.models.Store;

/** Driving port: entry point for updating a store (full replace). */
public interface UpdateStoreOperation {
  Store update(Long id, Store store);
}
