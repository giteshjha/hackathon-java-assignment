package com.fulfilment.application.monolith.observability;

import static org.junit.jupiter.api.Assertions.*;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;

/**
 * Unit test for FulfillmentMetrics.
 * Uses SimpleMeterRegistry (no Quarkus context required) to verify every counter and timer
 * is registered and incremented correctly.
 */
class FulfillmentMetricsTest {

  @Test
  void allCountersAndTimersAreRegisteredAndIncrementable() {
    SimpleMeterRegistry registry = new SimpleMeterRegistry();
    FulfillmentMetrics metrics = new FulfillmentMetrics(registry);

    // Invoke every public record method
    metrics.recordWarehouseCreated();
    metrics.recordWarehouseArchived();
    metrics.recordWarehouseReplaced();
    metrics.recordStoreCreated();
    metrics.recordStoreUpdated();
    metrics.recordStoreDeleted();
    metrics.recordProductCreated();
    metrics.recordProductUpdated();
    metrics.recordProductDeleted();

    // Each counter must have been incremented exactly once
    assertEquals(1.0, registry.counter("fulfillment.warehouse.created").count(),  "warehouse.created");
    assertEquals(1.0, registry.counter("fulfillment.warehouse.archived").count(), "warehouse.archived");
    assertEquals(1.0, registry.counter("fulfillment.warehouse.replaced").count(), "warehouse.replaced");
    assertEquals(1.0, registry.counter("fulfillment.store.created").count(),   "store.created");
    assertEquals(1.0, registry.counter("fulfillment.store.updated").count(),   "store.updated");
    assertEquals(1.0, registry.counter("fulfillment.store.deleted").count(),   "store.deleted");
    assertEquals(1.0, registry.counter("fulfillment.product.created").count(), "product.created");
    assertEquals(1.0, registry.counter("fulfillment.product.updated").count(), "product.updated");
    assertEquals(1.0, registry.counter("fulfillment.product.deleted").count(), "product.deleted");

    // Timer must be registered (count=0 since we never called it via record())
    assertNotNull(metrics.warehouseSearchTimer, "search timer should not be null");
  }

  @Test
  void multipleIncrementsAccumulate() {
    SimpleMeterRegistry registry = new SimpleMeterRegistry();
    FulfillmentMetrics metrics = new FulfillmentMetrics(registry);

    metrics.recordWarehouseCreated();
    metrics.recordWarehouseCreated();
    metrics.recordWarehouseCreated();

    assertEquals(3.0, registry.counter("fulfillment.warehouse.created").count());
  }
}
