package com.fulfilment.application.monolith.stores;

import com.fulfilment.application.monolith.products.Product;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

/**
 * Join entity representing the many-to-many relationship between a Store and its Products,
 * tracking how many units of each Product are stocked in a given Store.
 */
@Entity
@Table(name = "store_product",
       uniqueConstraints = @UniqueConstraint(columnNames = {"store_id", "product_id"}))
public class StoreProduct {

  @Id @GeneratedValue
  public Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "store_id", nullable = false)
  @OnDelete(action = OnDeleteAction.CASCADE)
  public Store store;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "product_id", nullable = false)
  @OnDelete(action = OnDeleteAction.CASCADE)
  public Product product;

  public int quantity;

  public StoreProduct() {}

  public StoreProduct(Store store, Product product, int quantity) {
    this.store = store;
    this.product = product;
    this.quantity = quantity;
  }
}
