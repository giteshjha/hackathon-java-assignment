package com.fulfilment.application.monolith.products.adapters.database;

import com.fulfilment.application.monolith.products.domain.models.Product;
import com.fulfilment.application.monolith.products.domain.ports.ProductStore;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.WebApplicationException;
import java.util.List;

/**
 * Driven adapter: implements {@link ProductStore} using Panache/JPA.
 * Translates between domain {@link Product} and persistence {@link DbProduct}.
 */
@ApplicationScoped
public class ProductRepository implements ProductStore, PanacheRepository<DbProduct> {

  @Override
  public List<Product> getAll(Sort sort) {
    return listAll(sort).stream().map(DbProduct::toProduct).toList();
  }

  @Override
  public Product getById(Long id) {
    DbProduct db = find("id", id).firstResult();
    return db != null ? db.toProduct() : null;
  }

  @Override
  public void create(Product product) {
    DbProduct db = new DbProduct();
    db.applyFrom(product);
    persist(db);
    product.id = db.id; // back-fill the generated id into the domain model
  }

  @Override
  public Product update(Long id, Product product) {
    DbProduct db = find("id", id).firstResult();
    if (db == null) {
      throw new WebApplicationException("Product with id of " + id + " does not exist.", 404);
    }
    db.applyFrom(product);
    return db.toProduct();
  }

  @Override
  public void delete(Long id) {
    DbProduct db = find("id", id).firstResult();
    if (db != null) {
      delete(db);
    }
  }

  /**
   * Adjusts available stock by {@code delta} within the caller's active transaction.
   * Finds the managed {@link DbProduct} entity directly so Hibernate dirty-checking
   * persists the change on commit — no explicit flush needed.
   *
   * @param id    product id
   * @param delta negative to decrement (allocate), positive to increment (return)
   */
  @Override
  public void adjustStock(Long id, int delta) {
    DbProduct db = getEntityManager().find(DbProduct.class, id);
    if (db == null) {
      throw new WebApplicationException("Product with id of " + id + " does not exist.", 404);
    }
    db.stock += delta;
  }

  // ── Convenience methods used by join-table adapters (stay typed to DbProduct) ─────────────────

  /** Returns the managed {@link DbProduct} JPA entity for use in join-table relationships. */
  public DbProduct findDbById(Long id) {
    return getEntityManager().find(DbProduct.class, id);
  }
}
