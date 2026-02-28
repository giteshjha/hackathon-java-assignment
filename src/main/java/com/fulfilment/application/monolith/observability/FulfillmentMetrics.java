package com.fulfilment.application.monolith.observability;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

/**
 * Central observability bean for the Fulfillment system.
 *
 * <p>Exposes Micrometer counters and timers for all domain operations. Metrics are automatically
 * exported to Prometheus at {@code /q/metrics} by the {@code quarkus-micrometer-registry-prometheus}
 * extension. Scrape that endpoint from Prometheus with a standard {@code scrape_configs} job.
 *
 * <p>Wire-in example for a REST resource:
 * <pre>
 *   &#64;Inject FulfillmentMetrics metrics;
 *   // inside a method:
 *   metrics.recordWarehouseCreated();
 * </pre>
 *
 * <p>Prometheus scrape config (prometheus.yml):
 * <pre>
 *   scrape_configs:
 *     - job_name: 'fulfillment'
 *       metrics_path: '/q/metrics'
 *       static_configs:
 *         - targets: ['localhost:8080']
 * </pre>
 */
@ApplicationScoped
public class FulfillmentMetrics {

  // ── Warehouse counters ─────────────────────────────────────────────────────
  private final Counter warehouseCreated;
  private final Counter warehouseArchived;
  private final Counter warehouseReplaced;

  // ── Store counters ─────────────────────────────────────────────────────────
  private final Counter storeCreated;
  private final Counter storeUpdated;
  private final Counter storeDeleted;

  // ── Product counters ───────────────────────────────────────────────────────
  private final Counter productCreated;
  private final Counter productUpdated;
  private final Counter productDeleted;

  // ── Search timer ──────────────────────────────────────────────────────────
  /** Measures how long warehouse search queries take end-to-end (including DB). */
  public final Timer warehouseSearchTimer;

  @Inject
  public FulfillmentMetrics(MeterRegistry registry) {
    warehouseCreated = Counter.builder("fulfillment.warehouse.created")
        .description("Total number of warehouses successfully created")
        .register(registry);
    warehouseArchived = Counter.builder("fulfillment.warehouse.archived")
        .description("Total number of warehouses archived (soft-deleted)")
        .register(registry);
    warehouseReplaced = Counter.builder("fulfillment.warehouse.replaced")
        .description("Total number of warehouse replacement operations completed")
        .register(registry);

    storeCreated = Counter.builder("fulfillment.store.created")
        .description("Total number of stores created")
        .register(registry);
    storeUpdated = Counter.builder("fulfillment.store.updated")
        .description("Total number of store update operations (PUT or PATCH)")
        .register(registry);
    storeDeleted = Counter.builder("fulfillment.store.deleted")
        .description("Total number of stores deleted")
        .register(registry);

    productCreated = Counter.builder("fulfillment.product.created")
        .description("Total number of products created")
        .register(registry);
    productUpdated = Counter.builder("fulfillment.product.updated")
        .description("Total number of product update operations")
        .register(registry);
    productDeleted = Counter.builder("fulfillment.product.deleted")
        .description("Total number of products deleted")
        .register(registry);

    warehouseSearchTimer = Timer.builder("fulfillment.warehouse.search.duration")
        .description("Latency of warehouse search queries")
        .register(registry);
  }

  // ── Public increment methods (call these from resources/use-cases) ─────────

  /** Increment when a warehouse is successfully created. */
  public void recordWarehouseCreated() { warehouseCreated.increment(); }

  /** Increment when a warehouse is successfully archived. */
  public void recordWarehouseArchived() { warehouseArchived.increment(); }

  /** Increment when a warehouse replacement completes successfully. */
  public void recordWarehouseReplaced() { warehouseReplaced.increment(); }

  /** Increment when a store is successfully created. */
  public void recordStoreCreated() { storeCreated.increment(); }

  /** Increment when a store is successfully updated (PUT or PATCH). */
  public void recordStoreUpdated() { storeUpdated.increment(); }

  /** Increment when a store is successfully deleted. */
  public void recordStoreDeleted() { storeDeleted.increment(); }

  /** Increment when a product is successfully created. */
  public void recordProductCreated() { productCreated.increment(); }

  /** Increment when a product is successfully updated. */
  public void recordProductUpdated() { productUpdated.increment(); }

  /** Increment when a product is successfully deleted. */
  public void recordProductDeleted() { productDeleted.increment(); }
}
