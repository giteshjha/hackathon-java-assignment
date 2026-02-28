package com.fulfilment.application.monolith.stores.domain.models;

/** Pure domain model — no JPA or framework annotations. */
public class Store {

  public Long id;

  public String name;

  public int quantityProductsInStock;

  public Store() {}

  public Store(String name) {
    this.name = name;
  }
}
