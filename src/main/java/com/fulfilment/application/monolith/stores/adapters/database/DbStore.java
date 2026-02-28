package com.fulfilment.application.monolith.stores.adapters.database;

import com.fulfilment.application.monolith.stores.domain.models.Store;
import jakarta.persistence.Cacheable;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;

/**
 * JPA persistence entity for Store. Lives in the adapter layer — the domain model
 * {@link Store} has no JPA annotations. Use {@code @Entity(name = "Store")} so that
 * existing JPQL queries (e.g. {@code from Store}) continue to work unchanged.
 */
@Entity(name = "Store")
@Table(name = "store")
@Cacheable
public class DbStore {

  @Id @GeneratedValue
  public Long id;

  @Column(length = 40, unique = true)
  public String name;

  public int quantityProductsInStock;

  @com.fasterxml.jackson.annotation.JsonIgnore
  @OneToMany(mappedBy = "store", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
  public List<StoreProduct> products = new ArrayList<>();

  public DbStore() {}

  public DbStore(String name) {
    this.name = name;
  }

  /** Maps this JPA entity to the pure domain model. */
  public Store toStore() {
    Store s = new Store();
    s.id = this.id;
    s.name = this.name;
    s.quantityProductsInStock = this.quantityProductsInStock;
    return s;
  }

  /** Copies domain model fields onto this managed entity (does not set id or products list). */
  public void applyFrom(Store s) {
    this.name = s.name;
    this.quantityProductsInStock = s.quantityProductsInStock;
  }
}
