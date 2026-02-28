package com.fulfilment.application.monolith.products.adapters.database;

import jakarta.persistence.Cacheable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;

/**
 * JPA entity for Product. Also serves as the REST request/response model.
 * Use {@code @Entity(name = "Product")} so existing JPQL queries continue to work.
 */
@Entity(name = "Product")
@Table(name = "product")
@Cacheable
public class Product {

  @Id @GeneratedValue
  public Long id;

  @NotBlank(message = "Product name is required.")
  @Column(length = 40, unique = true, nullable = false)
  public String name;

  @Column(nullable = true)
  public String description;

  @DecimalMin(value = "0.0", inclusive = true, message = "Price must be zero or positive.")
  @Column(precision = 10, scale = 2, nullable = true)
  public BigDecimal price;

  @Min(value = 0, message = "Stock cannot be negative.")
  public int stock;

  public Product() {}

  public Product(String name) {
    this.name = name;
  }
}
