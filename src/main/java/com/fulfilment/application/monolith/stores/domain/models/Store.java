package com.fulfilment.application.monolith.stores.domain.models;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

/** Pure domain model — no JPA annotations. */
public class Store {

  public Long id;

  @NotBlank(message = "Store name is required.")
  public String name;

  @Min(value = 0, message = "Stock quantity cannot be negative.")
  public int quantityProductsInStock;

  public Store() {}

  public Store(String name) {
    this.name = name;
  }
}
