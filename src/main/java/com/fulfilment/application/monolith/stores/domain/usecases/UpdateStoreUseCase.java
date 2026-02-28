package com.fulfilment.application.monolith.stores.domain.usecases;

import com.fulfilment.application.monolith.stores.StoreUpdatedEvent;
import com.fulfilment.application.monolith.stores.domain.models.Store;
import com.fulfilment.application.monolith.stores.domain.ports.StoreRepository;
import com.fulfilment.application.monolith.stores.domain.ports.UpdateStoreOperation;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;

@ApplicationScoped
public class UpdateStoreUseCase implements UpdateStoreOperation {

  private final StoreRepository storeRepository;

  @Inject Event<StoreUpdatedEvent> storeUpdatedEvent;

  public UpdateStoreUseCase(StoreRepository storeRepository) {
    this.storeRepository = storeRepository;
  }

  @Override
  @Transactional
  public Store update(Long id, Store store) {
    if (store.name == null) {
      throw new WebApplicationException("Store Name was not set on request.", 422);
    }
    Store updated = storeRepository.update(id, store);
    storeRepository.synchronizeQuantity(id);
    updated = storeRepository.getById(id);
    storeUpdatedEvent.fire(new StoreUpdatedEvent(updated));
    return updated;
  }
}
