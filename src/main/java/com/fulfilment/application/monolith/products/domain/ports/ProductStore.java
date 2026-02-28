package com.fulfilment.application.monolith.products.domain.ports;

import com.fulfilment.application.monolith.products.domain.models.Product;
import io.quarkus.panache.common.Sort;
import java.util.List;

/** Driven port: persistence contract for the Product aggregate. */
public interface ProductStore {

  List<Product> getAll(Sort sort);

  Product getById(Long id);

  void create(Product product);

  Product update(Long id, Product product);

  void delete(Long id);

  /**
   * Atomically adjusts available stock by {@code delta} units (negative = decrement, positive =
   * increment). Must be called inside an active {@code @Transactional} boundary.
   */
  void adjustStock(Long id, int delta);
}
