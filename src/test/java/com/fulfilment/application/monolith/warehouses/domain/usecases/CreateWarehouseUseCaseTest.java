package com.fulfilment.application.monolith.warehouses.domain.usecases;

import static org.junit.jupiter.api.Assertions.*;

import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.LocationResolver;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CreateWarehouseUseCaseTest {

  private InMemoryWarehouseStore warehouseStore;
  private InMemoryLocationResolver locationResolver;
  private CreateWarehouseUseCase useCase;

  @BeforeEach
  void setup() {
    warehouseStore = new InMemoryWarehouseStore();
    locationResolver = new InMemoryLocationResolver();
    useCase = new CreateWarehouseUseCase(warehouseStore, locationResolver);
  }

  @Test
  void createSucceedsWhenInputIsValid() {
    Warehouse warehouse = warehouse("WH-001", "AMSTERDAM-001", 50, 10);

    useCase.create(warehouse);

    Warehouse saved = warehouseStore.findByBusinessUnitCode("WH-001");
    assertNotNull(saved);
    assertEquals("AMSTERDAM-001", saved.location);
    assertEquals(50, saved.capacity);
    assertEquals(10, saved.stock);
    assertNotNull(saved.createdAt);
  }

  @Test
  void createFailsWhenBusinessUnitCodeAlreadyExists() {
    warehouseStore.create(warehouse("WH-001", "AMSTERDAM-001", 50, 10));
    Warehouse duplicate = warehouse("WH-001", "AMSTERDAM-001", 30, 5);

    IllegalArgumentException exception =
        assertThrows(IllegalArgumentException.class, () -> useCase.create(duplicate));

    assertTrue(exception.getMessage().contains("already exists"));
  }

  @Test
  void createFailsWhenLocationIsInvalid() {
    Warehouse warehouse = warehouse("WH-001", "UNKNOWN-001", 30, 5);

    IllegalArgumentException exception =
        assertThrows(IllegalArgumentException.class, () -> useCase.create(warehouse));

    assertTrue(exception.getMessage().contains("is not valid"));
  }

  @Test
  void createFailsWhenCapacityExceedsLocationLimit() {
    Warehouse warehouse = warehouse("WH-001", "ZWOLLE-001", 41, 5);

    IllegalArgumentException exception =
        assertThrows(IllegalArgumentException.class, () -> useCase.create(warehouse));

    assertTrue(exception.getMessage().contains("exceeds location max capacity"));
  }

  @Test
  void createFailsWhenStockExceedsWarehouseCapacity() {
    Warehouse warehouse = warehouse("WH-001", "AMSTERDAM-001", 30, 31);

    IllegalArgumentException exception =
        assertThrows(IllegalArgumentException.class, () -> useCase.create(warehouse));

    assertTrue(exception.getMessage().contains("exceeds warehouse capacity"));
  }

  private Warehouse warehouse(String code, String location, int capacity, int stock) {
    Warehouse warehouse = new Warehouse();
    warehouse.businessUnitCode = code;
    warehouse.location = location;
    warehouse.capacity = capacity;
    warehouse.stock = stock;
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
      throw new UnsupportedOperationException("Not used in this test");
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
      copy.createdAt = warehouse.createdAt;
      copy.archivedAt = warehouse.archivedAt;
      return copy;
    }
  }
}
