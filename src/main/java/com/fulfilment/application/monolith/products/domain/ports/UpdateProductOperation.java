package com.fulfilment.application.monolith.products.domain.ports;

import com.fulfilment.application.monolith.products.domain.models.Product;

/** Driving port: entry point for updating a product. */
public interface UpdateProductOperation {
  Product update(Long id, Product product);
}
