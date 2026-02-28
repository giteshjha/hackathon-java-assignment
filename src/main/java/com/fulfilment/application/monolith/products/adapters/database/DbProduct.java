package com.fulfilment.application.monolith.products.adapters.database;

import com.fulfilment.application.monolith.products.domain.models.Product;
import jakarta.persistence.Cacheable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;

/**
 * JPA persistence entity for Product. Lives in the adapter layer — the domain model
 * {@link Product} has no JPA annotations. Use {@code @Entity(name = "Product")} so that
 * existing JPQL queries (e.g. {@code from Product}) continue to work unchanged.
 */
@Entity(name = "Product")
@Table(name = "product")
@Cacheable
public class DbProduct {

  @Id @GeneratedValue
  public Long id;

  @Column(length = 40, unique = true)
  public String name;

  @Column(nullable = true)
  public String description;

  @Column(precision = 10, scale = 2, nullable = true)
  public BigDecimal price;

  /** Available (unallocated) stock units. */
  public int stock;

  public DbProduct() {}

  public DbProduct(String name) {
    this.name = name;
  }

  /** Maps this JPA entity to the pure domain model. */
  public Product toProduct() {
    Product p = new Product();
    p.id = this.id;
    p.name = this.name;
    p.description = this.description;
    p.price = this.price;
    p.stock = this.stock;
    return p;
  }

  /** Copies domain model fields onto this managed entity (does not set id). */
  public void applyFrom(Product p) {
    this.name = p.name;
    this.description = p.description;
    this.price = p.price;
    this.stock = p.stock;
  }
}
