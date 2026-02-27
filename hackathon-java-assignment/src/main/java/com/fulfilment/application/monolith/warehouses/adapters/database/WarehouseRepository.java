package com.fulfilment.application.monolith.warehouses.adapters.database;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

  @Transactional
  public List<Warehouse> searchActive(
      String location,
      Integer minCapacity,
      Integer maxCapacity,
      String sortBy,
      String sortOrder,
      int page,
      int pageSize) {
    StringBuilder query = new StringBuilder("archivedAt is null");
    Map<String, Object> params = new HashMap<>();

    if (location != null) {
      query.append(" and location = :location");
      params.put("location", location);
    }
    if (minCapacity != null) {
      query.append(" and capacity >= :minCapacity");
      params.put("minCapacity", minCapacity);
    }
    if (maxCapacity != null) {
      query.append(" and capacity <= :maxCapacity");
      params.put("maxCapacity", maxCapacity);
    }

    Sort sort = "capacity".equals(sortBy) ? Sort.by("capacity") : Sort.by("createdAt");
    if ("desc".equals(sortOrder)) {
      sort = sort.descending();
    }

    var panacheQuery = find(query.toString(), sort, params);
    panacheQuery.page(Page.of(page, pageSize));
    return panacheQuery.list().stream().map(DbWarehouse::toWarehouse).toList();
  }
}
