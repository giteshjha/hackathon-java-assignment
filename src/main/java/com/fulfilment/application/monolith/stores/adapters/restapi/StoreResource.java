package com.fulfilment.application.monolith.stores.adapters.restapi;

import com.fulfilment.application.monolith.products.adapters.database.Product;
import com.fulfilment.application.monolith.products.adapters.database.ProductRepository;
import com.fulfilment.application.monolith.stores.adapters.database.DbStore;
import com.fulfilment.application.monolith.stores.adapters.database.StoreProduct;
import com.fulfilment.application.monolith.stores.adapters.database.StoreRepositoryAdapter;
import com.fulfilment.application.monolith.stores.domain.models.Store;
import com.fulfilment.application.monolith.stores.domain.ports.CreateStoreOperation;
import com.fulfilment.application.monolith.stores.domain.ports.DeleteStoreOperation;
import com.fulfilment.application.monolith.stores.domain.ports.PatchStoreOperation;
import com.fulfilment.application.monolith.stores.domain.ports.UpdateStoreOperation;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import java.util.List;

/**
 * Driving adapter: thin REST endpoint that delegates CRUD to domain operation ports.
 * Product-mapping endpoints stay here as they are cross-aggregate and adapter-layer concerns.
 */
@Path("store")
@ApplicationScoped
@Produces("application/json")
@Consumes("application/json")
public class StoreResource {

  @Inject CreateStoreOperation createStoreOperation;
  @Inject UpdateStoreOperation updateStoreOperation;
  @Inject PatchStoreOperation patchStoreOperation;
  @Inject DeleteStoreOperation deleteStoreOperation;
  @Inject StoreRepositoryAdapter storeRepositoryAdapter;
  @Inject ProductRepository productRepository;
  @Inject EntityManager entityManager;

  @GET
  public List<Store> get() {
    return storeRepositoryAdapter.getAll();
  }

  @GET
  @Path("{id}")
  public Store getSingle(@PathParam("id") Long id) {
    Store entity = storeRepositoryAdapter.getById(id);
    if (entity == null) {
      throw new WebApplicationException("Store with id of " + id + " does not exist.", 404);
    }
    return entity;
  }

  @POST
  public Response create(@Valid Store store) {
    return createStoreOperation.create(store);
  }

  @PUT
  @Path("{id}")
  public Store update(@PathParam("id") Long id, @Valid Store store) {
    return updateStoreOperation.update(id, store);
  }

  @PATCH
  @Path("{id}")
  public Store patch(@PathParam("id") Long id, @Valid Store store) {
    return patchStoreOperation.patch(id, store);
  }

  @DELETE
  @Path("{id}")
  public Response delete(@PathParam("id") Long id) {
    return deleteStoreOperation.delete(id);
  }

  // ── Product mapping endpoints (adapter-layer, cross-aggregate) ───────────────────────────────

  @GET
  @Path("{id}/products")
  public List<StoreProductView> listStoreProducts(@PathParam("id") Long id) {
    DbStore store = requireDbStore(id);
    return entityManager
        .createQuery(
            "select sp from StoreProduct sp join fetch sp.product where sp.store = :store order by sp.product.name",
            StoreProduct.class)
        .setParameter("store", store)
        .getResultList()
        .stream()
        .map(StoreProductView::from)
        .toList();
  }

  @PUT
  @Path("{id}/products/{productId}")
  @Transactional
  public StoreProductView upsertStoreProduct(
      @PathParam("id") Long id,
      @PathParam("productId") Long productId,
      StoreProductRequest request) {
    if (request == null || request.quantity == null || request.quantity <= 0) {
      throw new WebApplicationException("Quantity must be a positive number.", 422);
    }

    DbStore store = requireDbStore(id);
    Product product = requireProduct(productId);

    StoreProduct existing =
        entityManager
            .createQuery(
                "select sp from StoreProduct sp where sp.store = :store and sp.product = :product",
                StoreProduct.class)
            .setParameter("store", store)
            .setParameter("product", product)
            .getResultStream()
            .findFirst()
            .orElse(null);

    if (existing == null) {
      if (product.stock < request.quantity) {
        throw new WebApplicationException(
            "Insufficient stock for '" + product.name + "'. Requested: " + request.quantity
                + ", Available: " + product.stock + ".", 422);
      }
      product.stock -= request.quantity;
      existing = new StoreProduct(store, product, request.quantity);
      entityManager.persist(existing);
    } else {
      int delta = request.quantity - existing.quantity;
      if (delta > 0 && product.stock < delta) {
        throw new WebApplicationException(
            "Insufficient stock for '" + product.name + "'. Needs " + delta + " more unit(s), Available: "
                + product.stock + ".", 422);
      }
      product.stock -= delta;
      existing.quantity = request.quantity;
    }

    synchronizeStoreQuantity(store);
    return StoreProductView.from(existing);
  }

  @DELETE
  @Path("{id}/products/{productId}")
  @Transactional
  public Response deleteStoreProduct(
      @PathParam("id") Long id, @PathParam("productId") Long productId) {
    DbStore store = requireDbStore(id);
    Product product = requireProduct(productId);

    StoreProduct existing =
        entityManager
            .createQuery(
                "select sp from StoreProduct sp where sp.store = :store and sp.product = :product",
                StoreProduct.class)
            .setParameter("store", store)
            .setParameter("product", product)
            .getResultStream()
            .findFirst()
            .orElse(null);

    if (existing == null) {
      throw new WebApplicationException(
          "Store-product mapping does not exist for store " + id + " and product " + productId + ".", 404);
    }

    product.stock += existing.quantity;
    entityManager.remove(existing);
    synchronizeStoreQuantity(store);
    return Response.status(204).build();
  }

  private DbStore requireDbStore(Long id) {
    DbStore store = storeRepositoryAdapter.findDbById(id);
    if (store == null) {
      throw new WebApplicationException("Store with id of " + id + " does not exist.", 404);
    }
    return store;
  }

  private Product requireProduct(Long id) {
    Product product = productRepository.findDbById(id);
    if (product == null) {
      throw new WebApplicationException("Product with id of " + id + " does not exist.", 404);
    }
    return product;
  }

  private void synchronizeStoreQuantity(DbStore store) {
    Number total =
        entityManager
            .createQuery(
                "select coalesce(sum(sp.quantity), 0) from StoreProduct sp where sp.store = :store",
                Number.class)
            .setParameter("store", store)
            .getSingleResult();
    store.quantityProductsInStock = total.intValue();
  }

  public static class StoreProductRequest {
    public Integer quantity;
  }

  public static class StoreProductView {
    public Long storeId;
    public Long productId;
    public String productName;
    public Integer quantity;

    static StoreProductView from(StoreProduct mapping) {
      StoreProductView view = new StoreProductView();
      view.storeId = mapping.store.id;
      view.productId = mapping.product.id;
      view.productName = mapping.product.name;
      view.quantity = mapping.quantity;
      return view;
    }
  }
}
