package com.fulfilment.application.monolith.products.domain.usecases;

import com.fulfilment.application.monolith.products.domain.models.Product;
import com.fulfilment.application.monolith.products.domain.ports.ProductStore;
import com.fulfilment.application.monolith.products.domain.ports.UpdateProductOperation;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;

@ApplicationScoped
public class UpdateProductUseCase implements UpdateProductOperation {

  private final ProductStore productStore;

  public UpdateProductUseCase(ProductStore productStore) {
    this.productStore = productStore;
  }

  @Override
  @Transactional
  public Product update(Long id, Product product) {
    if (product.name == null) {
      throw new WebApplicationException("Product Name was not set on request.", 422);
    }
    return productStore.update(id, product);
  }
}
