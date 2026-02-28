package com.fulfilment.application.monolith.stores.domain.ports;

import com.fulfilment.application.monolith.stores.domain.models.Store;
import java.util.List;

/** Driven port: persistence contract for the Store aggregate. */
public interface StoreRepository {

  List<Store> getAll();

  Store getById(Long id);

  void create(Store store);

  Store update(Long id, Store store);

  void delete(Long id);

  void synchronizeQuantity(Long id);
}
