package com.fulfilment.application.monolith.stores.adapters.database;

import com.fulfilment.application.monolith.stores.domain.models.Store;
import com.fulfilment.application.monolith.stores.domain.ports.StoreRepository;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import java.util.List;

/**
 * Driven adapter: implements {@link StoreRepository} using Panache/JPA.
 * Translates between domain {@link Store} and persistence {@link DbStore}.
 */
@ApplicationScoped
public class StoreRepositoryAdapter implements StoreRepository, PanacheRepository<DbStore> {

  @Inject EntityManager entityManager;

  @Override
  public List<Store> getAll() {
    return listAll(Sort.by("name")).stream().map(DbStore::toStore).toList();
  }

  @Override
  public Store getById(Long id) {
    DbStore db = getEntityManager().find(DbStore.class, id);
    return db != null ? db.toStore() : null;
  }

  @Override
  public void create(Store store) {
    DbStore db = new DbStore();
    db.applyFrom(store);
    persist(db);
    store.id = db.id; // back-fill generated id
  }

  @Override
  public Store update(Long id, Store store) {
    DbStore db = getEntityManager().find(DbStore.class, id);
    if (db == null) {
      throw new WebApplicationException("Store with id of " + id + " does not exist.", 404);
    }
    db.applyFrom(store);
    return db.toStore();
  }

  @Override
  public void delete(Long id) {
    DbStore db = getEntityManager().find(DbStore.class, id);
    if (db != null) {
      delete(db);
    }
  }

  @Override
  public void synchronizeQuantity(Long id) {
    Number total = entityManager
        .createQuery(
            "select coalesce(sum(sp.quantity), 0) from StoreProduct sp where sp.store.id = :id",
            Number.class)
        .setParameter("id", id)
        .getSingleResult();
    DbStore db = getEntityManager().find(DbStore.class, id);
    if (db != null) {
      db.quantityProductsInStock = total.intValue();
    }
  }

  /** Returns the managed {@link DbStore} JPA entity for use in join-table relationships. */
  public DbStore findDbById(Long id) {
    return getEntityManager().find(DbStore.class, id);
  }
}
