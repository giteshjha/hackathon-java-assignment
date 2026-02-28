package com.fulfilment.application.monolith.products.domain.models;

import java.math.BigDecimal;

/** Pure domain model — no JPA or framework annotations. */
public class Product {

  public Long id;

  public String name;

  public String description;

  public BigDecimal price;

  /** Available (unallocated) stock units. */
  public int stock;

  public Product() {}

  public Product(String name) {
    this.name = name;
  }
}
