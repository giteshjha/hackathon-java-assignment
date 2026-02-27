package com.fulfilment.application.monolith.warehouses.adapters.restapi;

import com.fulfilment.application.monolith.warehouses.adapters.database.WarehouseRepository;
import com.fulfilment.application.monolith.warehouses.domain.ports.ArchiveWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.CreateWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.ReplaceWarehouseOperation;
import com.warehouse.api.WarehouseResource;
import com.warehouse.api.beans.Warehouse;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.WebApplicationException;
import java.math.BigInteger;
import java.util.List;

@RequestScoped
public class WarehouseResourceImpl implements WarehouseResource {

  @Inject private WarehouseRepository warehouseRepository;
  @Inject private CreateWarehouseOperation createWarehouseOperation;
  @Inject private ArchiveWarehouseOperation archiveWarehouseOperation;
  @Inject private ReplaceWarehouseOperation replaceWarehouseOperation;

  @Override
  public List<Warehouse> listAllWarehousesUnits() {
    return warehouseRepository.getAll().stream().map(this::toWarehouseResponse).toList();
  }

  @Override
  @Transactional
  public Warehouse createANewWarehouseUnit(@NotNull Warehouse data) {
    // Convert API model to domain model
    var domainWarehouse = new com.fulfilment.application.monolith.warehouses.domain.models.Warehouse();
    domainWarehouse.businessUnitCode = data.getBusinessUnitCode();
    domainWarehouse.location = data.getLocation();
    domainWarehouse.capacity = data.getCapacity();
    domainWarehouse.stock = data.getStock() != null ? data.getStock() : 0;

    try {
      // Create warehouse through use case (includes validations)
      createWarehouseOperation.create(domainWarehouse);
      
      // Return the created warehouse
      return toWarehouseResponse(domainWarehouse);
    } catch (IllegalArgumentException e) {
      throw new WebApplicationException(e.getMessage(), 400);
    }
  }

  @Override
  public Warehouse getAWarehouseUnitByID(String id) {
    // Find warehouse by business unit code
    var domainWarehouse = warehouseRepository.findByBusinessUnitCode(id);
    
    if (domainWarehouse == null) {
      throw new WebApplicationException("Warehouse with business unit code '" + id + "' not found", 404);
    }
    
    return toWarehouseResponse(domainWarehouse);
  }

  @Override
  @Transactional
  public void archiveAWarehouseUnitByID(String id) {
    // Find warehouse by business unit code
    var domainWarehouse = warehouseRepository.findByBusinessUnitCode(id);

    if (domainWarehouse == null) {
      throw new WebApplicationException("Warehouse with business unit code '" + id + "' not found", 404);
    }

    try {
      // Archive warehouse through use case (includes validations)
      archiveWarehouseOperation.archive(domainWarehouse);
    } catch (IllegalArgumentException e) {
      throw new WebApplicationException(e.getMessage(), 400);
    }
  }

  @Override
  @Transactional
  public Warehouse replaceTheCurrentActiveWarehouse(
      String businessUnitCode, @NotNull Warehouse data) {
    // Convert API model to domain model
    var domainWarehouse = new com.fulfilment.application.monolith.warehouses.domain.models.Warehouse();
    domainWarehouse.businessUnitCode = businessUnitCode; // Use businessUnitCode from path
    domainWarehouse.location = data.getLocation();
    domainWarehouse.capacity = data.getCapacity();
    domainWarehouse.stock = data.getStock() != null ? data.getStock() : 0;

    try {
      // Replace warehouse through use case (includes validations)
      replaceWarehouseOperation.replace(domainWarehouse);

      // Return the updated warehouse
      var updated = warehouseRepository.findByBusinessUnitCode(businessUnitCode);
      return toWarehouseResponse(updated);
    } catch (IllegalArgumentException e) {
      throw new WebApplicationException(e.getMessage(), 400);
    }
  }

  @Override
  public List<Warehouse> searchWarehouses(
      String location,
      BigInteger minCapacity,
      BigInteger maxCapacity,
      String sortBy,
      String sortOrder,
      BigInteger page,
      BigInteger pageSize) {
    String normalizedSortBy = sortBy == null ? "createdAt" : sortBy;
    String normalizedSortOrder = sortOrder == null ? "asc" : sortOrder;
    int normalizedPage = toIntOrDefault(page, 0, "page");
    int normalizedPageSize = toIntOrDefault(pageSize, 10, "pageSize");
    Integer normalizedMinCapacity = minCapacity == null ? null : toInt(minCapacity, "minCapacity");
    Integer normalizedMaxCapacity = maxCapacity == null ? null : toInt(maxCapacity, "maxCapacity");
    String normalizedLocation = location == null || location.isBlank() ? null : location;

    if (!"createdAt".equals(normalizedSortBy) && !"capacity".equals(normalizedSortBy)) {
      throw new WebApplicationException("sortBy must be either 'createdAt' or 'capacity'", 400);
    }
    if (!"asc".equals(normalizedSortOrder) && !"desc".equals(normalizedSortOrder)) {
      throw new WebApplicationException("sortOrder must be either 'asc' or 'desc'", 400);
    }
    if (normalizedPage < 0) {
      throw new WebApplicationException("page must be >= 0", 400);
    }
    if (normalizedPageSize < 1 || normalizedPageSize > 100) {
      throw new WebApplicationException("pageSize must be between 1 and 100", 400);
    }
    if (normalizedMinCapacity != null && normalizedMinCapacity < 0) {
      throw new WebApplicationException("minCapacity must be >= 0", 400);
    }
    if (normalizedMaxCapacity != null && normalizedMaxCapacity < 0) {
      throw new WebApplicationException("maxCapacity must be >= 0", 400);
    }
    if (normalizedMinCapacity != null
        && normalizedMaxCapacity != null
        && normalizedMinCapacity > normalizedMaxCapacity) {
      throw new WebApplicationException("minCapacity must be <= maxCapacity", 400);
    }

    return warehouseRepository.searchActive(
            normalizedLocation,
            normalizedMinCapacity,
            normalizedMaxCapacity,
            normalizedSortBy,
            normalizedSortOrder,
            normalizedPage,
            normalizedPageSize)
        .stream()
        .map(this::toWarehouseResponse)
        .toList();
  }

  private int toIntOrDefault(BigInteger value, int defaultValue, String fieldName) {
    return value == null ? defaultValue : toInt(value, fieldName);
  }

  private int toInt(BigInteger value, String fieldName) {
    try {
      return value.intValueExact();
    } catch (ArithmeticException e) {
      throw new WebApplicationException(fieldName + " is out of supported integer range", 400);
    }
  }

  private Warehouse toWarehouseResponse(
      com.fulfilment.application.monolith.warehouses.domain.models.Warehouse warehouse) {
    var response = new Warehouse();
    response.setBusinessUnitCode(warehouse.businessUnitCode);
    response.setLocation(warehouse.location);
    response.setCapacity(warehouse.capacity);
    response.setStock(warehouse.stock);

    return response;
  }
}
