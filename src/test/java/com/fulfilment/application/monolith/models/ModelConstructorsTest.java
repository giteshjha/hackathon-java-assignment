package com.fulfilment.application.monolith.models;

import static org.junit.jupiter.api.Assertions.*;

import com.fulfilment.application.monolith.products.domain.models.Product;
import com.fulfilment.application.monolith.stores.domain.models.Store;
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
