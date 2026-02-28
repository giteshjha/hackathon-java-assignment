package com.fulfilment.application.monolith.products.domain.usecases;

import com.fulfilment.application.monolith.products.adapters.database.Product;
import com.fulfilment.application.monolith.products.domain.ports.DeleteProductOperation;
import com.fulfilment.application.monolith.products.domain.ports.ProductStore;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

@ApplicationScoped
public class DeleteProductUseCase implements DeleteProductOperation {

  private final ProductStore productStore;

  public DeleteProductUseCase(ProductStore productStore) {
    this.productStore = productStore;
  }

  @Override
  @Transactional
  public Response delete(Long id) {
    Product product = productStore.getById(id);
    if (product == null) {
      throw new WebApplicationException("Product with id of " + id + " does not exist.", 404);
    }
    productStore.delete(id);
    return Response.status(204).build();
  }
}
