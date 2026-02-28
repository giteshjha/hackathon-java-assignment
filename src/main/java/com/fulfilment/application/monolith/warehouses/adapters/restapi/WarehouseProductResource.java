package com.fulfilment.application.monolith.warehouses.adapters.restapi;

import com.fulfilment.application.monolith.products.adapters.database.Product;
import com.fulfilment.application.monolith.products.adapters.database.ProductRepository;
import com.fulfilment.application.monolith.warehouses.adapters.database.DbWarehouse;
import com.fulfilment.application.monolith.warehouses.adapters.database.WarehouseProduct;
import com.fulfilment.application.monolith.warehouses.adapters.database.WarehouseRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import java.util.List;

@Path("warehouse/{businessUnitCode}/products")
@ApplicationScoped
@Produces("application/json")
@Consumes("application/json")
public class WarehouseProductResource {

  @Inject WarehouseRepository warehouseRepository;
  @Inject ProductRepository productRepository;
  @Inject EntityManager entityManager;

  @GET
  public List<WarehouseProductView> listWarehouseProducts(
      @PathParam("businessUnitCode") String businessUnitCode) {
    DbWarehouse warehouse = requireWarehouse(businessUnitCode);
    return entityManager
        .createQuery(
            "select wp from WarehouseProduct wp join fetch wp.product where wp.warehouse = :warehouse order by wp.product.name",
            WarehouseProduct.class)
        .setParameter("warehouse", warehouse)
        .getResultList()
        .stream()
        .map(WarehouseProductView::from)
        .toList();
  }

  @PUT
  @Path("{productId}")
  @Transactional
  public WarehouseProductView upsertWarehouseProduct(
      @PathParam("businessUnitCode") String businessUnitCode,
      @PathParam("productId") Long productId,
      WarehouseProductRequest request) {
    if (request == null || request.quantity == null || request.quantity <= 0) {
      throw new WebApplicationException("Quantity must be a positive number.", 422);
    }

    DbWarehouse warehouse = requireWarehouse(businessUnitCode);
    ensureWarehouseIsActive(warehouse);
    Product product = requireProduct(productId);

    WarehouseProduct existing =
        entityManager
            .createQuery(
                "select wp from WarehouseProduct wp where wp.warehouse = :warehouse and wp.product = :product",
                WarehouseProduct.class)
            .setParameter("warehouse", warehouse)
            .setParameter("product", product)
            .getResultStream()
            .findFirst()
            .orElse(null);

    if (existing == null) {
      if (product.stock < request.quantity) {
        throw new WebApplicationException(
            "Insufficient stock for '" + product.name + "'. Requested: " + request.quantity
                + ", Available: " + product.stock + ".", 422);
      }
      product.stock -= request.quantity;
      existing = new WarehouseProduct(warehouse, product, request.quantity);
      entityManager.persist(existing);
    } else {
      int delta = request.quantity - existing.quantity;
      if (delta > 0 && product.stock < delta) {
        throw new WebApplicationException(
            "Insufficient stock for '" + product.name + "'. Needs " + delta + " more unit(s), Available: "
                + product.stock + ".", 422);
      }
      product.stock -= delta;   // negative delta returns stock
      existing.quantity = request.quantity;
    }

    synchronizeWarehouseStock(warehouse);
    return WarehouseProductView.from(existing);
  }

  @DELETE
  @Path("{productId}")
  @Transactional
  public Response deleteWarehouseProduct(
      @PathParam("businessUnitCode") String businessUnitCode,
      @PathParam("productId") Long productId) {
    DbWarehouse warehouse = requireWarehouse(businessUnitCode);
    ensureWarehouseIsActive(warehouse);
    Product product = requireProduct(productId);

    WarehouseProduct existing =
        entityManager
            .createQuery(
                "select wp from WarehouseProduct wp where wp.warehouse = :warehouse and wp.product = :product",
                WarehouseProduct.class)
            .setParameter("warehouse", warehouse)
            .setParameter("product", product)
            .getResultStream()
            .findFirst()
            .orElse(null);

    if (existing == null) {
      throw new WebApplicationException(
          "Warehouse-product mapping does not exist for warehouse '"
              + businessUnitCode
              + "' and product "
              + productId
              + ".",
          404);
    }

    product.stock += existing.quantity;   // return allocated units to available pool
    entityManager.remove(existing);
    synchronizeWarehouseStock(warehouse);
    return Response.status(204).build();
  }

  private DbWarehouse requireWarehouse(String businessUnitCode) {
    DbWarehouse warehouse = warehouseRepository.find("businessUnitCode", businessUnitCode).firstResult();
    if (warehouse == null) {
      throw new WebApplicationException(
          "Warehouse with business unit code '" + businessUnitCode + "' not found", 404);
    }
    return warehouse;
  }

  private Product requireProduct(Long id) {
    Product product = productRepository.findDbById(id);
    if (product == null) {
      throw new WebApplicationException("Product with id of " + id + " does not exist.", 404);
    }
    return product;
  }

  private void ensureWarehouseIsActive(DbWarehouse warehouse) {
    if (warehouse.archivedAt != null) {
      throw new WebApplicationException(
          "Warehouse with business unit code '" + warehouse.businessUnitCode + "' is archived.", 409);
    }
  }

  private void synchronizeWarehouseStock(DbWarehouse warehouse) {
    Number total =
        entityManager
            .createQuery(
                "select coalesce(sum(wp.quantity), 0) from WarehouseProduct wp where wp.warehouse = :warehouse",
                Number.class)
            .setParameter("warehouse", warehouse)
            .getSingleResult();

    int totalStock = total.intValue();
    if (warehouse.capacity != null && totalStock > warehouse.capacity) {
      throw new WebApplicationException(
          "Total product quantity (" + totalStock + ") exceeds warehouse capacity (" + warehouse.capacity + ").",
          422);
    }

    warehouse.stock = totalStock;
  }

  public static class WarehouseProductRequest {
    public Integer quantity;
  }

  public static class WarehouseProductView {
    public String warehouseBusinessUnitCode;
    public Long productId;
    public String productName;
    public Integer quantity;

    static WarehouseProductView from(WarehouseProduct mapping) {
      WarehouseProductView view = new WarehouseProductView();
      view.warehouseBusinessUnitCode = mapping.warehouse.businessUnitCode;
      view.productId = mapping.product.id;
      view.productName = mapping.product.name;
      view.quantity = mapping.quantity;
      return view;
    }
  }
}
