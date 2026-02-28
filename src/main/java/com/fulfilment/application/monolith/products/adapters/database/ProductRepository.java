package com.fulfilment.application.monolith.products.adapters.database;

import com.fulfilment.application.monolith.products.domain.ports.ProductStore;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.WebApplicationException;
import java.util.List;

/**
 * Driven adapter: implements {@link ProductStore} using Panache/JPA.
 */
@ApplicationScoped
public class ProductRepository implements ProductStore, PanacheRepository<Product> {

  @Override
  public List<Product> getAll(Sort sort) {
    return listAll(sort);
  }

  @Override
  public Product getById(Long id) {
    return find("id", id).firstResult();
  }

  @Override
  public void create(Product product) {
    product.id = null; // always let @GeneratedValue assign the id
    persist(product);
  }

  @Override
  public Product update(Long id, Product product) {
    Product db = find("id", id).firstResult();
    if (db == null) {
      throw new WebApplicationException("Product with id of " + id + " does not exist.", 404);
    }
    db.name = product.name;
    db.description = product.description;
    db.price = product.price;
    db.stock = product.stock;
    return db;
  }

  @Override
  public void delete(Long id) {
    Product db = find("id", id).firstResult();
    if (db != null) {
      delete(db);
    }
  }

  @Override
  public void adjustStock(Long id, int delta) {
    Product db = getEntityManager().find(Product.class, id);
    if (db == null) {
      throw new WebApplicationException("Product with id of " + id + " does not exist.", 404);
    }
    db.stock += delta;
  }

  /** Returns the managed JPA entity for use in join-table relationships. */
  public Product findDbById(Long id) {
    return getEntityManager().find(Product.class, id);
  }
}
