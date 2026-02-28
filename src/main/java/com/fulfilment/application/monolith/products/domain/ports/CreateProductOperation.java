package com.fulfilment.application.monolith.products.domain.ports;

import com.fulfilment.application.monolith.products.domain.models.Product;
import jakarta.ws.rs.core.Response;

/** Driving port: entry point for creating a product. */
public interface CreateProductOperation {
  Response create(Product product);
}
