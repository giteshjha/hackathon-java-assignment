package com.fulfilment.application.monolith.products.adapters.restapi;

import com.fulfilment.application.monolith.products.domain.models.Product;
import com.fulfilment.application.monolith.products.domain.ports.CreateProductOperation;
import com.fulfilment.application.monolith.products.domain.ports.DeleteProductOperation;
import com.fulfilment.application.monolith.products.domain.ports.ProductStore;
import com.fulfilment.application.monolith.products.domain.ports.UpdateProductOperation;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import java.util.List;

/**
 * Driving adapter: thin REST endpoint that delegates all business logic to domain operation ports.
 */
@Path("product")
@ApplicationScoped
@Produces("application/json")
@Consumes("application/json")
public class ProductResource {

  @Inject ProductStore productStore;
  @Inject CreateProductOperation createProductOperation;
  @Inject UpdateProductOperation updateProductOperation;
  @Inject DeleteProductOperation deleteProductOperation;

  @GET
  public List<Product> get() {
    return productStore.getAll(Sort.by("name"));
  }

  @GET
  @Path("{id}")
  public Product getSingle(@PathParam("id") Long id) {
    Product entity = productStore.getById(id);
    if (entity == null) {
      throw new WebApplicationException("Product with id of " + id + " does not exist.", 404);
    }
    return entity;
  }

  @POST
  public Response create(Product product) {
    return createProductOperation.create(product);
  }

  @PUT
  @Path("{id}")
  public Product update(@PathParam("id") Long id, Product product) {
    return updateProductOperation.update(id, product);
  }

  @DELETE
  @Path("{id}")
  public Response delete(@PathParam("id") Long id) {
    return deleteProductOperation.delete(id);
  }
}
