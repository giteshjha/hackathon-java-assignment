package com.fulfilment.application.monolith.warehouses.domain.usecases;

import static org.junit.jupiter.api.Assertions.*;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ArchiveWarehouseUseCaseUnitTest {

  private InMemoryWarehouseStore warehouseStore;
  private ArchiveWarehouseUseCase useCase;

  @BeforeEach
  void setup() {
    warehouseStore = new InMemoryWarehouseStore();
    useCase = new ArchiveWarehouseUseCase(warehouseStore);
  }

  @Test
  void archiveSucceedsForActiveWarehouse() {
    Warehouse warehouse = warehouse("WH-001", null);
    warehouseStore.create(warehouse);

    useCase.archive(warehouse("WH-001", null));

    Warehouse updated = warehouseStore.findByBusinessUnitCode("WH-001");
    assertNotNull(updated.archivedAt);
  }

  @Test
  void archiveFailsWhenWarehouseDoesNotExist() {
    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class, () -> useCase.archive(warehouse("WH-404", null)));

    assertTrue(exception.getMessage().contains("does not exist"));
  }

  @Test
  void archiveFailsWhenWarehouseAlreadyArchived() {
    warehouseStore.create(warehouse("WH-001", LocalDateTime.now().minusDays(1)));

    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class, () -> useCase.archive(warehouse("WH-001", null)));

    assertTrue(exception.getMessage().contains("already archived"));
  }

  private Warehouse warehouse(String code, LocalDateTime archivedAt) {
    Warehouse warehouse = new Warehouse();
    warehouse.businessUnitCode = code;
    warehouse.archivedAt = archivedAt;
    return warehouse;
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
      copy.archivedAt = warehouse.archivedAt;
      return copy;
    }
  }
}
