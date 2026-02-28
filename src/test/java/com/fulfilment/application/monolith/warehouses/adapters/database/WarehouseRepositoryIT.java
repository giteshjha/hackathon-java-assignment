package com.fulfilment.application.monolith.warehouses.adapters.database;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import io.quarkus.test.junit.QuarkusTest;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

import jakarta.inject.Inject;

@QuarkusTest
class WarehouseRepositoryIT {

  @Inject WarehouseRepository warehouseRepository;

  @Test
  void createFindUpdateAndRemoveShouldWork() {
    String code = "IT-" + UUID.randomUUID();

    Warehouse created = warehouse(code, "ZWOLLE-001", 100, 10, LocalDateTime.now(), null);
    warehouseRepository.create(created);

    Warehouse found = warehouseRepository.findByBusinessUnitCode(code);
    assertNotNull(found);
    assertEquals("ZWOLLE-001", found.location);

    found.location = "AMSTERDAM-001";
    found.capacity = 120;
    warehouseRepository.update(found);

    Warehouse updated = warehouseRepository.findByBusinessUnitCode(code);
    assertNotNull(updated);
    assertEquals("AMSTERDAM-001", updated.location);
    assertEquals(120, updated.capacity);

    warehouseRepository.remove(updated);
    assertNull(warehouseRepository.findByBusinessUnitCode(code));
  }

  @Test
  void searchActiveShouldFilterSortAndPaginate() {
    String prefix = "IT-S-" + UUID.randomUUID();
    Warehouse first = warehouse(prefix + "-1", "AMSTERDAM-001", 20, 5, LocalDateTime.now().minusDays(3), null);
    Warehouse second = warehouse(prefix + "-2", "AMSTERDAM-001", 80, 7, LocalDateTime.now().minusDays(1), null);
    Warehouse archived =
        warehouse(
            prefix + "-3",
            "AMSTERDAM-001",
            90,
            2,
            LocalDateTime.now().minusDays(2),
            LocalDateTime.now());

    warehouseRepository.create(first);
    warehouseRepository.create(second);
    warehouseRepository.create(archived);

    List<Warehouse> filtered =
        warehouseRepository.searchActive(
            "AMSTERDAM-001", 10, 85, "capacity", "desc", 0, 10);

    assertTrue(filtered.stream().noneMatch(w -> w.businessUnitCode.equals(prefix + "-3")));
    assertTrue(filtered.stream().allMatch(w -> "AMSTERDAM-001".equals(w.location)));
    assertEquals(prefix + "-2", filtered.get(0).businessUnitCode);

    List<Warehouse> paged =
        warehouseRepository.searchActive(
            "AMSTERDAM-001", 0, 200, "capacity", "desc", 0, 1);

    assertEquals(1, paged.size());
    assertEquals(prefix + "-2", paged.get(0).businessUnitCode);

    warehouseRepository.remove(first);
    warehouseRepository.remove(second);
    warehouseRepository.remove(archived);
  }

  private static Warehouse warehouse(
      String code,
      String location,
      int capacity,
      int stock,
      LocalDateTime createdAt,
      LocalDateTime archivedAt) {
    Warehouse warehouse = new Warehouse();
    warehouse.businessUnitCode = code;
    warehouse.location = location;
    warehouse.capacity = capacity;
    warehouse.stock = stock;
    warehouse.createdAt = createdAt;
    warehouse.archivedAt = archivedAt;
    return warehouse;
  }
}
