package com.fulfilment.application.monolith.warehouses.adapters.database;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import java.util.List;

@ApplicationScoped
public class WarehouseRepository implements WarehouseStore, PanacheRepository<DbWarehouse> {

  @Override
  public List<Warehouse> getAll() {
    return this.listAll().stream().map(DbWarehouse::toWarehouse).toList();
  }

  @Override
  @Transactional
  public void create(Warehouse warehouse) {
    DbWarehouse dbWarehouse = new DbWarehouse();
    dbWarehouse.businessUnitCode = warehouse.businessUnitCode;
    dbWarehouse.location = warehouse.location;
    dbWarehouse.capacity = warehouse.capacity;
    dbWarehouse.stock = warehouse.stock;
    dbWarehouse.createdAt = warehouse.createdAt;
    dbWarehouse.archivedAt = warehouse.archivedAt;
    
    this.persist(dbWarehouse);
  }

  @Override
  @Transactional
  public void update(Warehouse warehouse) {
    DbWarehouse existing = find("businessUnitCode", warehouse.businessUnitCode).firstResult();
    if (existing == null) {
      throw new IllegalArgumentException(
          "Warehouse with business unit code '" + warehouse.businessUnitCode + "' does not exist");
    }

    existing.location = warehouse.location;
    existing.capacity = warehouse.capacity;
    existing.stock = warehouse.stock;
    // Preserve archived state unless the caller explicitly sets an archive timestamp.
    if (warehouse.archivedAt != null) {
      existing.archivedAt = warehouse.archivedAt;
    }

    getEntityManager().flush();
  }

  @Override
  @Transactional
  public void remove(Warehouse warehouse) {
    delete("businessUnitCode", warehouse.businessUnitCode);
  }

  @Override
  @Transactional
  public Warehouse findByBusinessUnitCode(String buCode) {
    DbWarehouse dbWarehouse = find("businessUnitCode", buCode).firstResult();
    return dbWarehouse != null ? dbWarehouse.toWarehouse() : null;
  }
}
