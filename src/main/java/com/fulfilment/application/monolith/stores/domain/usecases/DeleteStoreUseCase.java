package com.fulfilment.application.monolith.stores.domain.usecases;

import com.fulfilment.application.monolith.stores.StoreDeletedEvent;
import com.fulfilment.application.monolith.stores.domain.models.Store;
import com.fulfilment.application.monolith.stores.domain.ports.DeleteStoreOperation;
import com.fulfilment.application.monolith.stores.domain.ports.StoreRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

@ApplicationScoped
public class DeleteStoreUseCase implements DeleteStoreOperation {

  private final StoreRepository storeRepository;

  @Inject Event<StoreDeletedEvent> storeDeletedEvent;

  public DeleteStoreUseCase(StoreRepository storeRepository) {
    this.storeRepository = storeRepository;
  }

  @Override
  @Transactional
  public Response delete(Long id) {
    Store store = storeRepository.getById(id);
    if (store == null) {
      throw new WebApplicationException("Store with id of " + id + " does not exist.", 404);
    }
    storeRepository.delete(id);
    storeDeletedEvent.fire(new StoreDeletedEvent(store));
    return Response.status(204).build();
  }
}
