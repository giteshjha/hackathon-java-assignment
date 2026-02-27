package com.fulfilment.application.monolith.warehouses.domain.usecases;

import static org.junit.jupiter.api.Assertions.*;

import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.LocationResolver;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ReplaceWarehouseUseCaseUnitTest {

  private InMemoryWarehouseStore warehouseStore;
  private InMemoryLocationResolver locationResolver;
  private ReplaceWarehouseUseCase useCase;

  @BeforeEach
  void setup() {
    warehouseStore = new InMemoryWarehouseStore();
    locationResolver = new InMemoryLocationResolver();
    useCase = new ReplaceWarehouseUseCase(warehouseStore, locationResolver);
  }

  @Test
  void replaceSucceedsForValidInput() {
    warehouseStore.create(warehouse("WH-001", "AMSTERDAM-001", 50, 10, null));
    Warehouse replacement = warehouse("WH-001", "ZWOLLE-001", 40, 20, null);

    useCase.replace(replacement);

    Warehouse updated = warehouseStore.findByBusinessUnitCode("WH-001");
    assertEquals("ZWOLLE-001", updated.location);
    assertEquals(40, updated.capacity);
    assertEquals(20, updated.stock);
  }

  @Test
  void replaceFailsWhenWarehouseDoesNotExist() {
    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> useCase.replace(warehouse("WH-404", "AMSTERDAM-001", 30, 5, null)));

    assertTrue(exception.getMessage().contains("does not exist"));
  }

  @Test
  void replaceFailsWhenWarehouseIsArchived() {
    warehouseStore.create(
        warehouse("WH-001", "AMSTERDAM-001", 50, 10, LocalDateTime.now().minusDays(1)));

    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> useCase.replace(warehouse("WH-001", "ZWOLLE-001", 30, 5, null)));

    assertTrue(exception.getMessage().contains("is archived"));
  }

  @Test
  void replaceFailsWhenLocationIsInvalid() {
    warehouseStore.create(warehouse("WH-001", "AMSTERDAM-001", 50, 10, null));

    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> useCase.replace(warehouse("WH-001", "UNKNOWN-001", 30, 5, null)));

    assertTrue(exception.getMessage().contains("is not valid"));
  }

  @Test
  void replaceFailsWhenCapacityExceedsLocationLimit() {
    warehouseStore.create(warehouse("WH-001", "AMSTERDAM-001", 50, 10, null));

    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> useCase.replace(warehouse("WH-001", "ZWOLLE-001", 41, 5, null)));

    assertTrue(exception.getMessage().contains("exceeds location max capacity"));
  }

  @Test
  void replaceFailsWhenStockExceedsCapacity() {
    warehouseStore.create(warehouse("WH-001", "AMSTERDAM-001", 50, 10, null));

    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> useCase.replace(warehouse("WH-001", "ZWOLLE-001", 30, 31, null)));

    assertTrue(exception.getMessage().contains("exceeds warehouse capacity"));
  }

  private Warehouse warehouse(
      String code, String location, int capacity, int stock, LocalDateTime archivedAt) {
    Warehouse warehouse = new Warehouse();
    warehouse.businessUnitCode = code;
    warehouse.location = location;
    warehouse.capacity = capacity;
    warehouse.stock = stock;
    warehouse.archivedAt = archivedAt;
    return warehouse;
  }

  private static class InMemoryLocationResolver implements LocationResolver {
    @Override
    public Location resolveByIdentifier(String identifier) {
      if ("AMSTERDAM-001".equals(identifier)) {
        return new Location("AMSTERDAM-001", 5, 100);
      }
      if ("ZWOLLE-001".equals(identifier)) {
        return new Location("ZWOLLE-001", 1, 40);
      }
      return null;
    }
  }

  private static class InMemoryWarehouseStore implements WarehouseStore {
    private final List<Warehouse> warehouses = new ArrayList<>();

    @Override
    public List<Warehouse> getAll() {
      return warehouses;
    }

    @Override
    public void create(Warehouse warehouse) {
      warehouses.add(copyOf(warehouse));
    }

    @Override
    public void update(Warehouse warehouse) {
      for (int i = 0; i < warehouses.size(); i++) {
        if (warehouse.businessUnitCode.equals(warehouses.get(i).businessUnitCode)) {
          warehouses.set(i, copyOf(warehouse));
          return;
        }
      }
      throw new IllegalArgumentException("Warehouse not found");
    }

    @Override
    public void remove(Warehouse warehouse) {
      throw new UnsupportedOperationException("Not used in this test");
    }

    @Override
    public Warehouse findByBusinessUnitCode(String buCode) {
      return warehouses.stream()
          .filter(warehouse -> buCode.equals(warehouse.businessUnitCode))
          .findFirst()
          .map(this::copyOf)
          .orElse(null);
    }

    private Warehouse copyOf(Warehouse warehouse) {
      Warehouse copy = new Warehouse();
      copy.businessUnitCode = warehouse.businessUnitCode;
      copy.location = warehouse.location;
      copy.capacity = warehouse.capacity;
      copy.stock = warehouse.stock;
      copy.archivedAt = warehouse.archivedAt;
      return copy;
    }
  }
}
