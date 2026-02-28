package com.fulfilment.application.monolith.stores.domain.usecases;

import com.fulfilment.application.monolith.stores.StoreUpdatedEvent;
import com.fulfilment.application.monolith.stores.domain.models.Store;
import com.fulfilment.application.monolith.stores.domain.ports.PatchStoreOperation;
import com.fulfilment.application.monolith.stores.domain.ports.StoreRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;

@ApplicationScoped
public class PatchStoreUseCase implements PatchStoreOperation {

  private final StoreRepository storeRepository;

  @Inject Event<StoreUpdatedEvent> storeUpdatedEvent;

  public PatchStoreUseCase(StoreRepository storeRepository) {
    this.storeRepository = storeRepository;
  }

  @Override
  @Transactional
  public Store patch(Long id, Store patch) {
    if (patch.name == null) {
      throw new WebApplicationException("Store Name was not set on request.", 422);
    }
    Store existing = storeRepository.getById(id);
    if (existing == null) {
      throw new WebApplicationException("Store with id of " + id + " does not exist.", 404);
    }
    existing.name = patch.name;
    storeRepository.update(id, existing);
    storeRepository.synchronizeQuantity(id);
    existing = storeRepository.getById(id);
    storeUpdatedEvent.fire(new StoreUpdatedEvent(existing));
    return existing;
  }
}
