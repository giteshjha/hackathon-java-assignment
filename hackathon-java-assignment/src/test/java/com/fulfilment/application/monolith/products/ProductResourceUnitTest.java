package com.fulfilment.application.monolith.products;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ProductResourceUnitTest {

  private ProductRepository productRepository;
  private ProductResource resource;

  @BeforeEach
  void setup() throws Exception {
    productRepository = mock(ProductRepository.class);
    resource = new ProductResource();
    inject(resource, "productRepository", productRepository);
  }

  @Test
  void getReturnsRepositoryResults() {
    Product product = new Product("P1");
    when(productRepository.listAll(any())).thenReturn(List.of(product));

    List<Product> products = resource.get();

    assertEquals(1, products.size());
    assertEquals("P1", products.get(0).name);
  }

  @Test
  void getSingleThrows404WhenMissing() {
    when(productRepository.findById(99L)).thenReturn(null);

    WebApplicationException exception =
        assertThrows(WebApplicationException.class, () -> resource.getSingle(99L));

    assertEquals(404, exception.getResponse().getStatus());
  }

  @Test
  void createRejectsPresetId() {
    Product product = new Product("P1");
    product.id = 1L;

    WebApplicationException exception =
        assertThrows(WebApplicationException.class, () -> resource.create(product));

    assertEquals(422, exception.getResponse().getStatus());
  }

  @Test
  void createPersistsAndReturnsCreated() {
    Product product = new Product("P1");

    Response response = resource.create(product);

    verify(productRepository).persist(product);
    assertEquals(201, response.getStatus());
  }

  @Test
  void updateRejectsMissingName() {
    Product payload = new Product();
    payload.name = null;

    WebApplicationException exception =
        assertThrows(WebApplicationException.class, () -> resource.update(1L, payload));

    assertEquals(422, exception.getResponse().getStatus());
  }

  @Test
  void updateRejectsMissingEntity() {
    Product payload = new Product("Updated");
    when(productRepository.findById(1L)).thenReturn(null);

    WebApplicationException exception =
        assertThrows(WebApplicationException.class, () -> resource.update(1L, payload));

    assertEquals(404, exception.getResponse().getStatus());
  }

  @Test
  void updatePersistsModifiedEntity() {
    Product existing = new Product("Old");
    existing.description = "Old desc";
    existing.price = BigDecimal.ONE;
    existing.stock = 1;
    Product payload = new Product("New");
    payload.description = "New desc";
    payload.price = new BigDecimal("12.50");
    payload.stock = 42;
    when(productRepository.findById(1L)).thenReturn(existing);

    Product result = resource.update(1L, payload);

    assertEquals("New", result.name);
    assertEquals("New desc", result.description);
    assertEquals(new BigDecimal("12.50"), result.price);
    assertEquals(42, result.stock);
    verify(productRepository).persist(existing);
  }

  @Test
  void deleteRejectsMissingEntity() {
    when(productRepository.findById(1L)).thenReturn(null);

    WebApplicationException exception =
        assertThrows(WebApplicationException.class, () -> resource.delete(1L));

    assertEquals(404, exception.getResponse().getStatus());
  }

  @Test
  void deleteRemovesEntity() {
    Product existing = new Product("P1");
    when(productRepository.findById(1L)).thenReturn(existing);

    Response response = resource.delete(1L);

    verify(productRepository).delete(existing);
    assertEquals(204, response.getStatus());
  }

  @Test
  void errorMapperBuildsWebExceptionPayload() throws Exception {
    ProductResource.ErrorMapper mapper = new ProductResource.ErrorMapper();
    inject(mapper, "objectMapper", new ObjectMapper());

    Response response =
        mapper.toResponse(new WebApplicationException("Bad request", 400));

    assertEquals(400, response.getStatus());
    assertNotNull(response.getEntity());
    assertTrue(response.getEntity().toString().contains("Bad request"));
  }

  @Test
  void errorMapperBuildsGenericErrorPayload() throws Exception {
    ProductResource.ErrorMapper mapper = new ProductResource.ErrorMapper();
    inject(mapper, "objectMapper", new ObjectMapper());

    Response response = mapper.toResponse(new RuntimeException("boom"));

    assertEquals(500, response.getStatus());
    assertNotNull(response.getEntity());
    assertTrue(response.getEntity().toString().contains("boom"));
  }

  private static void inject(Object target, String fieldName, Object value) throws Exception {
    Field field = target.getClass().getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(target, value);
  }
}
