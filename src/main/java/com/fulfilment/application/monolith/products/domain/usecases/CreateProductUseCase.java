package com.fulfilment.application.monolith.products.domain.usecases;

import com.fulfilment.application.monolith.products.domain.models.Product;
import com.fulfilment.application.monolith.products.domain.ports.CreateProductOperation;
import com.fulfilment.application.monolith.products.domain.ports.ProductStore;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

@ApplicationScoped
public class CreateProductUseCase implements CreateProductOperation {

  private final ProductStore productStore;

  public CreateProductUseCase(ProductStore productStore) {
    this.productStore = productStore;
  }

  @Override
  @Transactional
  public Response create(Product product) {
    if (product.id != null) {
      throw new WebApplicationException("Id was invalidly set on request.", 422);
    }
    productStore.create(product);
    return Response.ok(product).status(201).build();
  }
}
