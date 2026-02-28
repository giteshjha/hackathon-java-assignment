package com.fulfilment.application.monolith.stores.domain.ports;

import jakarta.ws.rs.core.Response;

/** Driving port: entry point for deleting a store. */
public interface DeleteStoreOperation {
  Response delete(Long id);
}
