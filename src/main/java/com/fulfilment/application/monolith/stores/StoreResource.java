package com.fulfilment.application.monolith.stores;

import com.fulfilment.application.monolith.products.Product;
import com.fulfilment.application.monolith.products.ProductRepository;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
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

@Path("store")
@ApplicationScoped
@Produces("application/json")
@Consumes("application/json")
public class StoreResource {

  @Inject LegacyStoreManagerGateway legacyStoreManagerGateway;
  @Inject ProductRepository productRepository;
  @Inject EntityManager entityManager;

  @Inject Event<StoreCreatedEvent> storeCreatedEvent;

  @Inject Event<StoreUpdatedEvent> storeUpdatedEvent;

  @Inject Event<StoreDeletedEvent> storeDeletedEvent;

  @GET
  public List<Store> get() {
    return Store.listAll(Sort.by("name"));
  }

  @GET
  @Path("{id}")
  public Store getSingle(Long id) {
    Store entity = Store.findById(id);
    if (entity == null) {
      throw new WebApplicationException("Store with id of " + id + " does not exist.", 404);
    }
    return entity;
  }

  @POST
  @Transactional
  public Response create(Store store) {
    if (store.id != null) {
      throw new WebApplicationException("Id was invalidly set on request.", 422);
    }

    store.persist();
    storeCreatedEvent.fire(new StoreCreatedEvent(store));

    return Response.ok(store).status(201).build();
  }

  @PUT
  @Path("{id}")
  @Transactional
  public Store update(Long id, Store updatedStore) {
    if (updatedStore.name == null) {
      throw new WebApplicationException("Store Name was not set on request.", 422);
    }

    Store entity = Store.findById(id);

    if (entity == null) {
      throw new WebApplicationException("Store with id of " + id + " does not exist.", 404);
    }

    entity.name = updatedStore.name;
    updateQuantityConsistently(entity, updatedStore.quantityProductsInStock);

    storeUpdatedEvent.fire(new StoreUpdatedEvent(entity));

    return entity;
  }

  @PATCH
  @Path("{id}")
  @Transactional
  public Store patch(Long id, Store updatedStore) {
    if (updatedStore.name == null) {
      throw new WebApplicationException("Store Name was not set on request.", 422);
    }

    Store entity = Store.findById(id);

    if (entity == null) {
      throw new WebApplicationException("Store with id of " + id + " does not exist.", 404);
    }

    if (entity.name != null) {
      entity.name = updatedStore.name;
    }

    updateQuantityConsistently(entity, updatedStore.quantityProductsInStock);

    storeUpdatedEvent.fire(new StoreUpdatedEvent(entity));

    return entity;
  }

  @DELETE
  @Path("{id}")
  @Transactional
  public Response delete(Long id) {
    Store entity = Store.findById(id);
    if (entity == null) {
      throw new WebApplicationException("Store with id of " + id + " does not exist.", 404);
    }
    entity.delete();
    storeDeletedEvent.fire(new StoreDeletedEvent(entity));
    return Response.status(204).build();
  }

  @GET
  @Path("{id}/products")
  public List<StoreProductView> listStoreProducts(@PathParam("id") Long id) {
    Store store = requireStore(id);
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
      @PathParam("id") Long id, @PathParam("productId") Long productId, StoreProductRequest request) {
    if (request == null || request.quantity == null || request.quantity <= 0) {
      throw new WebApplicationException("Quantity must be a positive number.", 422);
    }

    Store store = requireStore(id);
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
      product.stock -= delta;   // negative delta returns stock
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
    Store store = requireStore(id);
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

    product.stock += existing.quantity;   // return allocated units to available pool
    entityManager.remove(existing);
    synchronizeStoreQuantity(store);
    return Response.status(204).build();
  }

  private Store requireStore(Long id) {
    Store store = Store.findById(id);
    if (store == null) {
      throw new WebApplicationException("Store with id of " + id + " does not exist.", 404);
    }
    return store;
  }

  private Product requireProduct(Long id) {
    Product product = productRepository.findById(id);
    if (product == null) {
      throw new WebApplicationException("Product with id of " + id + " does not exist.", 404);
    }
    return product;
  }

  /** Ensures the total units of a product allocated across ALL stores + warehouses never exceeds
   *  the product's own stock. existingMapping is the current row being replaced (null for new). */
  private void synchronizeStoreQuantity(Store store) {
    Number total =
        entityManager
            .createQuery(
                "select coalesce(sum(sp.quantity), 0) from StoreProduct sp where sp.store = :store",
                Number.class)
            .setParameter("store", store)
            .getSingleResult();
    store.quantityProductsInStock = total.intValue();
  }

  /** If the store has product mappings, always recalculate from mappings (ignores provided value).
   *  If no mappings exist, use the manually provided value (backward-compatible). */
  private void updateQuantityConsistently(Store store, int providedQuantity) {
    Number mappingCount =
        entityManager
            .createQuery(
                "select count(sp) from StoreProduct sp where sp.store = :store",
                Number.class)
            .setParameter("store", store)
            .getSingleResult();
    if (mappingCount.longValue() > 0) {
      synchronizeStoreQuantity(store);
    } else {
      store.quantityProductsInStock = providedQuantity;
    }
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
