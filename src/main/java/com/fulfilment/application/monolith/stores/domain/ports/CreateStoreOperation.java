package com.fulfilment.application.monolith.stores.domain.ports;

import com.fulfilment.application.monolith.stores.domain.models.Store;
import jakarta.ws.rs.core.Response;

/** Driving port: entry point for creating a store. */
public interface CreateStoreOperation {
  Response create(Store store);
}
