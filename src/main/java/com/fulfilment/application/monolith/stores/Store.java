package com.fulfilment.application.monolith.stores;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Cacheable;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;

@Entity
@Cacheable
public class Store extends PanacheEntity {

  @Column(length = 40, unique = true)
  public String name;

  public int quantityProductsInStock;

  /** Products stocked in this store, with per-product quantities. */
  @JsonIgnore
  @OneToMany(mappedBy = "store", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
  public List<StoreProduct> products = new ArrayList<>();

  public Store() {}

  public Store(String name) {
    this.name = name;
  }
}
