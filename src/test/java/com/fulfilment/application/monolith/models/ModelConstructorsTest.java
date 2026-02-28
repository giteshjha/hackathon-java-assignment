package com.fulfilment.application.monolith.models;

import static org.junit.jupiter.api.Assertions.*;

import com.fulfilment.application.monolith.products.Product;
import com.fulfilment.application.monolith.stores.Store;
import com.warehouse.api.beans.Warehouse;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class ModelConstructorsTest {

  @Test
  void productAndStoreConstructorsSetName() {
    Product product = new Product("P1");
    Store store = new Store("S1");

    assertEquals("P1", product.name);
    assertEquals("S1", store.name);
  }

  @Test
  void generatedWarehouseBeanGettersAndSettersWork() {
    Warehouse warehouse = new Warehouse();
    warehouse.setId("1");
    warehouse.setBusinessUnitCode("WH-001");
    warehouse.setLocation("AMSTERDAM-001");
    warehouse.setCapacity(100);
    warehouse.setStock(20);

    assertEquals("1", warehouse.getId());
    assertEquals("WH-001", warehouse.getBusinessUnitCode());
    assertEquals("AMSTERDAM-001", warehouse.getLocation());
    assertEquals(100, warehouse.getCapacity());
    assertEquals(20, warehouse.getStock());
  }

  @Test
  void productFieldsAreWritable() {
    Product product = new Product();
    product.description = "desc";
    product.price = new BigDecimal("9.99");
    product.stock = 3;

    assertEquals("desc", product.description);
    assertEquals(new BigDecimal("9.99"), product.price);
    assertEquals(3, product.stock);
  }
}
