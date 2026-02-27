package com.fulfilment.application.monolith.stores;

import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import java.lang.reflect.Field;
import org.junit.jupiter.api.Test;

class StoreSupportUnitTest {

  @Test
  void errorMapperBuildsWebExceptionPayload() throws Exception {
    StoreResource.ErrorMapper mapper = new StoreResource.ErrorMapper();
    inject(mapper, "objectMapper", new ObjectMapper());

    Response response = mapper.toResponse(new WebApplicationException("Bad request", 400));

    assertEquals(400, response.getStatus());
    assertNotNull(response.getEntity());
    assertTrue(response.getEntity().toString().contains("Bad request"));
  }

  @Test
  void errorMapperBuildsGenericPayload() throws Exception {
    StoreResource.ErrorMapper mapper = new StoreResource.ErrorMapper();
    inject(mapper, "objectMapper", new ObjectMapper());

    Response response = mapper.toResponse(new RuntimeException("boom"));

    assertEquals(500, response.getStatus());
    assertNotNull(response.getEntity());
    assertTrue(response.getEntity().toString().contains("boom"));
  }

  @Test
  void legacyGatewayCanWriteForCreateAndUpdate() {
    LegacyStoreManagerGateway gateway = new LegacyStoreManagerGateway();
    Store store = new Store("LEGACY-STORE-UNIT");
    store.quantityProductsInStock = 5;

    assertDoesNotThrow(() -> gateway.createStoreOnLegacySystem(store));
    assertDoesNotThrow(() -> gateway.updateStoreOnLegacySystem(store));
  }

  private static void inject(Object target, String fieldName, Object value) throws Exception {
    Field field = target.getClass().getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(target, value);
  }
}
