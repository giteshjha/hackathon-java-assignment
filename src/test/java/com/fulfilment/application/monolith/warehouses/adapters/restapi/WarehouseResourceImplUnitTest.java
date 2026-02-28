package com.fulfilment.application.monolith.warehouses.adapters.restapi;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.fulfilment.application.monolith.warehouses.adapters.database.WarehouseRepository;
import com.fulfilment.application.monolith.warehouses.domain.ports.ArchiveWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.CreateWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.ReplaceWarehouseOperation;
import com.warehouse.api.beans.Warehouse;
import jakarta.ws.rs.WebApplicationException;
import java.lang.reflect.Field;
import java.math.BigInteger;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class WarehouseResourceImplUnitTest {

  private WarehouseRepository warehouseRepository;
  private CreateWarehouseOperation createWarehouseOperation;
  private ArchiveWarehouseOperation archiveWarehouseOperation;
  private ReplaceWarehouseOperation replaceWarehouseOperation;
  private WarehouseResourceImpl resource;

  @BeforeEach
  void setup() throws Exception {
    warehouseRepository = mock(WarehouseRepository.class);
    createWarehouseOperation = mock(CreateWarehouseOperation.class);
    archiveWarehouseOperation = mock(ArchiveWarehouseOperation.class);
    replaceWarehouseOperation = mock(ReplaceWarehouseOperation.class);

    resource = new WarehouseResourceImpl();
    inject(resource, "warehouseRepository", warehouseRepository);
    inject(resource, "createWarehouseOperation", createWarehouseOperation);
    inject(resource, "archiveWarehouseOperation", archiveWarehouseOperation);
    inject(resource, "replaceWarehouseOperation", replaceWarehouseOperation);
  }

  @Test
  void listAllReturnsMappedWarehouses() {
    com.fulfilment.application.monolith.warehouses.domain.models.Warehouse domain =
        domain("WH-001", "AMSTERDAM-001", 100, 10);
    when(warehouseRepository.getAll()).thenReturn(List.of(domain));

    List<Warehouse> response = resource.listAllWarehousesUnits();

    assertEquals(1, response.size());
    assertEquals("WH-001", response.get(0).getBusinessUnitCode());
    assertEquals("AMSTERDAM-001", response.get(0).getLocation());
  }

  @Test
  void createBuildsDomainAndReturnsResponse() {
    Warehouse payload = api("WH-001", "AMSTERDAM-001", 100, null);

    Warehouse response = resource.createANewWarehouseUnit(payload);

    verify(createWarehouseOperation)
        .create(
            argThat(
                warehouse ->
                    "WH-001".equals(warehouse.businessUnitCode)
                        && "AMSTERDAM-001".equals(warehouse.location)
                        && warehouse.capacity == 100
                        && warehouse.stock == 0));
    assertEquals("WH-001", response.getBusinessUnitCode());
    assertEquals(0, response.getStock());
  }

  @Test
  void createReturns400OnValidationError() {
    Warehouse payload = api("WH-001", "INVALID", 100, 0);
    doThrow(new IllegalArgumentException("invalid"))
        .when(createWarehouseOperation)
        .create(any());

    WebApplicationException exception =
        assertThrows(WebApplicationException.class, () -> resource.createANewWarehouseUnit(payload));

    assertEquals(400, exception.getResponse().getStatus());
  }

  @Test
  void getReturns404WhenMissing() {
    when(warehouseRepository.findByBusinessUnitCode("WH-404")).thenReturn(null);

    WebApplicationException exception =
        assertThrows(WebApplicationException.class, () -> resource.getAWarehouseUnitByID("WH-404"));

    assertEquals(404, exception.getResponse().getStatus());
  }

  @Test
  void archiveReturns404WhenMissing() {
    when(warehouseRepository.findByBusinessUnitCode("WH-404")).thenReturn(null);

    WebApplicationException exception =
        assertThrows(
            WebApplicationException.class, () -> resource.archiveAWarehouseUnitByID("WH-404"));

    assertEquals(404, exception.getResponse().getStatus());
  }

  @Test
  void archiveReturns400OnValidationError() {
    when(warehouseRepository.findByBusinessUnitCode("WH-001"))
        .thenReturn(domain("WH-001", "AMSTERDAM-001", 100, 1));
    doThrow(new IllegalArgumentException("already archived"))
        .when(archiveWarehouseOperation)
        .archive(any());

    WebApplicationException exception =
        assertThrows(
            WebApplicationException.class, () -> resource.archiveAWarehouseUnitByID("WH-001"));

    assertEquals(400, exception.getResponse().getStatus());
  }

  @Test
  void replaceReturnsUpdatedWarehouse() {
    Warehouse payload = api("IGNORED", "ZWOLLE-001", 40, 5);
    when(warehouseRepository.findByBusinessUnitCode("WH-001"))
        .thenReturn(domain("WH-001", "ZWOLLE-001", 40, 5));

    Warehouse response = resource.replaceTheCurrentActiveWarehouse("WH-001", payload);

    verify(replaceWarehouseOperation)
        .replace(
            argThat(
                warehouse ->
                    "WH-001".equals(warehouse.businessUnitCode)
                        && "ZWOLLE-001".equals(warehouse.location)
                        && warehouse.capacity == 40
                        && warehouse.stock == 5));
    assertEquals("WH-001", response.getBusinessUnitCode());
    assertEquals("ZWOLLE-001", response.getLocation());
  }

  @Test
  void replaceReturns400OnValidationError() {
    Warehouse payload = api("IGNORED", "ZWOLLE-001", 40, 5);
    doThrow(new IllegalArgumentException("invalid")).when(replaceWarehouseOperation).replace(any());

    WebApplicationException exception =
        assertThrows(
            WebApplicationException.class,
            () -> resource.replaceTheCurrentActiveWarehouse("WH-001", payload));

    assertEquals(400, exception.getResponse().getStatus());
  }

  @Test
  void searchReturnsMappedResultsUsingDefaults() {
    com.fulfilment.application.monolith.warehouses.domain.models.Warehouse domain =
        domain("WH-001", "ZWOLLE-001", 100, 10);
    when(warehouseRepository.searchActive(null, null, null, "createdAt", "asc", 0, 10))
        .thenReturn(List.of(domain));

    List<Warehouse> response =
        resource.searchWarehouses(null, null, null, null, null, null, null);

    assertEquals(1, response.size());
    assertEquals("WH-001", response.get(0).getBusinessUnitCode());
  }

  @Test
  void searchRejectsUnsupportedSortBy() {
    WebApplicationException exception =
        assertThrows(
            WebApplicationException.class,
            () ->
                resource.searchWarehouses(
                    null, null, null, "name", "asc", BigInteger.ZERO, BigInteger.TEN));

    assertEquals(400, exception.getResponse().getStatus());
  }

  @Test
  void searchRejectsInvalidSortOrder() {
    WebApplicationException exception =
        assertThrows(
            WebApplicationException.class,
            () ->
                resource.searchWarehouses(
                    null, null, null, "createdAt", "up", BigInteger.ZERO, BigInteger.TEN));

    assertEquals(400, exception.getResponse().getStatus());
  }

  @Test
  void searchRejectsNegativePage() {
    WebApplicationException exception =
        assertThrows(
            WebApplicationException.class,
            () ->
                resource.searchWarehouses(
                    null, null, null, "createdAt", "asc", BigInteger.valueOf(-1), BigInteger.TEN));

    assertEquals(400, exception.getResponse().getStatus());
  }

  @Test
  void searchRejectsInvalidCapacityRange() {
    WebApplicationException exception =
        assertThrows(
            WebApplicationException.class,
            () ->
                resource.searchWarehouses(
                    null,
                    BigInteger.valueOf(10),
                    BigInteger.valueOf(5),
                    "createdAt",
                    "asc",
                    BigInteger.ZERO,
                    BigInteger.TEN));

    assertEquals(400, exception.getResponse().getStatus());
  }

  @Test
  void searchRejectsValuesOutsideIntegerRange() {
    WebApplicationException exception =
        assertThrows(
            WebApplicationException.class,
            () ->
                resource.searchWarehouses(
                    null,
                    null,
                    null,
                    "createdAt",
                    "asc",
                    BigInteger.valueOf(Integer.MAX_VALUE).add(BigInteger.ONE),
                    BigInteger.TEN));

    assertEquals(400, exception.getResponse().getStatus());
  }

  private static Warehouse api(String code, String location, Integer capacity, Integer stock) {
    Warehouse warehouse = new Warehouse();
    warehouse.setBusinessUnitCode(code);
    warehouse.setLocation(location);
    warehouse.setCapacity(capacity);
    warehouse.setStock(stock);
    return warehouse;
  }

  private static com.fulfilment.application.monolith.warehouses.domain.models.Warehouse domain(
      String code, String location, Integer capacity, Integer stock) {
    com.fulfilment.application.monolith.warehouses.domain.models.Warehouse warehouse =
        new com.fulfilment.application.monolith.warehouses.domain.models.Warehouse();
    warehouse.businessUnitCode = code;
    warehouse.location = location;
    warehouse.capacity = capacity;
    warehouse.stock = stock;
    return warehouse;
  }

  private static void inject(Object target, String fieldName, Object value) throws Exception {
    Field field = target.getClass().getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(target, value);
  }
}
