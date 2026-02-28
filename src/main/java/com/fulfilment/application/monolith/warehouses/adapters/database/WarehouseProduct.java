package com.fulfilment.application.monolith.warehouses.adapters.database;

import com.fulfilment.application.monolith.products.adapters.database.Product;
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
 * Join entity representing the many-to-many relationship between a Warehouse and its Products,
 * tracking how many units of each Product are stored in a given Warehouse.
 */
@Entity
@Table(name = "warehouse_product",
       uniqueConstraints = @UniqueConstraint(columnNames = {"warehouse_id", "product_id"}))
public class WarehouseProduct {

  @Id @GeneratedValue
  public Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "warehouse_id", nullable = false)
  @OnDelete(action = OnDeleteAction.CASCADE)
  public DbWarehouse warehouse;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "product_id", nullable = false)
  @OnDelete(action = OnDeleteAction.CASCADE)
  public Product product;

  public int quantity;

  public WarehouseProduct() {}

  public WarehouseProduct(DbWarehouse warehouse, Product product, int quantity) {
    this.warehouse = warehouse;
    this.product = product;
    this.quantity = quantity;
  }
}
