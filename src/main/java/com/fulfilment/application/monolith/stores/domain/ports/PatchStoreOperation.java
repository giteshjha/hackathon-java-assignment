package com.fulfilment.application.monolith.stores.domain.ports;

import com.fulfilment.application.monolith.stores.domain.models.Store;

/** Driving port: entry point for partially updating a store (PATCH). */
public interface PatchStoreOperation {
  Store patch(Long id, Store store);
}
