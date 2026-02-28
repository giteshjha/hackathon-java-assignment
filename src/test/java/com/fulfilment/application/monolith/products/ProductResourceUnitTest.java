package com.fulfilment.application.monolith.products;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.fulfilment.application.monolith.products.adapters.restapi.ProductResource;
import com.fulfilment.application.monolith.products.domain.models.Product;
import com.fulfilment.application.monolith.products.domain.ports.ProductStore;
import com.fulfilment.application.monolith.products.domain.usecases.CreateProductUseCase;
import com.fulfilment.application.monolith.products.domain.usecases.DeleteProductUseCase;
import com.fulfilment.application.monolith.products.domain.usecases.UpdateProductUseCase;
import io.quarkus.panache.common.Sort;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for the product hexagonal layer.
 * Tests the use cases (where business logic lives) and the thin REST resource.
 */
class ProductResourceUnitTest {

  // ── Use-case unit tests ────────────────────────────────────────────────────

  private ProductStore productStore;
  private CreateProductUseCase createUseCase;
  private UpdateProductUseCase updateUseCase;
  private DeleteProductUseCase deleteUseCase;

  @BeforeEach
  void setup() throws Exception {
    productStore = mock(ProductStore.class);
    createUseCase = new CreateProductUseCase(productStore);
    updateUseCase = new UpdateProductUseCase(productStore);
    deleteUseCase = new DeleteProductUseCase(productStore);
  }

  @Test
  void createRejectsPresetId() {
    Product product = new Product("P1");
    product.id = 1L;

    WebApplicationException exception =
        assertThrows(WebApplicationException.class, () -> createUseCase.create(product));

    assertEquals(422, exception.getResponse().getStatus());
  }

  @Test
  void createPersistsAndReturnsCreated() {
    Product product = new Product("P1");

    Response response = createUseCase.create(product);

    verify(productStore).create(product);
    assertEquals(201, response.getStatus());
  }

  @Test
  void updateRejectsMissingName() {
    Product payload = new Product();
    payload.name = null;

    WebApplicationException exception =
        assertThrows(WebApplicationException.class, () -> updateUseCase.update(1L, payload));

    assertEquals(422, exception.getResponse().getStatus());
  }

  @Test
  void updateRejectsMissingEntity() {
    Product payload = new Product("Updated");
    when(productStore.update(1L, payload))
        .thenThrow(new WebApplicationException("Product with id of 1 does not exist.", 404));

    WebApplicationException exception =
        assertThrows(WebApplicationException.class, () -> updateUseCase.update(1L, payload));

    assertEquals(404, exception.getResponse().getStatus());
  }

  @Test
  void updateDelegatesToStore() {
    Product payload = new Product("New");
    payload.description = "New desc";
    payload.price = new BigDecimal("12.50");
    payload.stock = 42;
    Product updated = new Product("New");
    updated.description = "New desc";
    updated.price = new BigDecimal("12.50");
    updated.stock = 42;
    when(productStore.update(1L, payload)).thenReturn(updated);

    Product result = updateUseCase.update(1L, payload);

    assertEquals("New", result.name);
    assertEquals(42, result.stock);
    verify(productStore).update(1L, payload);
  }

  @Test
  void deleteRejectsMissingEntity() {
    when(productStore.getById(1L)).thenReturn(null);

    WebApplicationException exception =
        assertThrows(WebApplicationException.class, () -> deleteUseCase.delete(1L));

    assertEquals(404, exception.getResponse().getStatus());
  }

  @Test
  void deleteRemovesEntity() {
    Product existing = new Product("P1");
    when(productStore.getById(1L)).thenReturn(existing);

    Response response = deleteUseCase.delete(1L);

    verify(productStore).delete(1L);
    assertEquals(204, response.getStatus());
  }

  // ── Resource-level tests ───────────────────────────────────────────────────

  @Test
  void resourceGetDelegatesToStore() throws Exception {
    ProductResource resource = new ProductResource();
    Product product = new Product("P1");
    ProductStore store = mock(ProductStore.class);
    when(store.getAll(any(Sort.class))).thenReturn(List.of(product));
    injectField(resource, "productStore", store);

    List<Product> products = resource.get();

    assertEquals(1, products.size());
    assertEquals("P1", products.get(0).name);
  }

  @Test
  void resourceGetSingleThrows404WhenMissing() throws Exception {
    ProductResource resource = new ProductResource();
    ProductStore store = mock(ProductStore.class);
    when(store.getById(99L)).thenReturn(null);
    injectField(resource, "productStore", store);

    WebApplicationException exception =
        assertThrows(WebApplicationException.class, () -> resource.getSingle(99L));

    assertEquals(404, exception.getResponse().getStatus());
  }

  private static void injectField(Object target, String fieldName, Object value) throws Exception {
    Field field = target.getClass().getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(target, value);
  }
}
