package com.fulfilment.application.monolith.stores.domain.usecases;

import com.fulfilment.application.monolith.stores.StoreCreatedEvent;
import com.fulfilment.application.monolith.stores.domain.models.Store;
import com.fulfilment.application.monolith.stores.domain.ports.CreateStoreOperation;
import com.fulfilment.application.monolith.stores.domain.ports.StoreRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

@ApplicationScoped
public class CreateStoreUseCase implements CreateStoreOperation {

  private final StoreRepository storeRepository;

  @Inject Event<StoreCreatedEvent> storeCreatedEvent;

  public CreateStoreUseCase(StoreRepository storeRepository) {
    this.storeRepository = storeRepository;
  }

  @Override
  @Transactional
  public Response create(Store store) {
    if (store.id != null) {
      throw new WebApplicationException("Id was invalidly set on request.", 422);
    }
    storeRepository.create(store);
    storeCreatedEvent.fire(new StoreCreatedEvent(store));
    return Response.ok(store).status(201).build();
  }
}
