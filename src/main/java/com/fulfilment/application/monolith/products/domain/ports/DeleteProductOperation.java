package com.fulfilment.application.monolith.products.domain.ports;

import jakarta.ws.rs.core.Response;

/** Driving port: entry point for deleting a product. */
public interface DeleteProductOperation {
  Response delete(Long id);
}
