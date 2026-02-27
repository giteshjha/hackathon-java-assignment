package com.fulfilment.application.monolith.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import java.lang.reflect.Field;
import org.junit.jupiter.api.Test;

class ApiExceptionMapperTest {

  @Test
  void buildsWebApplicationExceptionPayload() throws Exception {
    ApiExceptionMapper mapper = new ApiExceptionMapper();
    inject(mapper, "objectMapper", new ObjectMapper());

    Response response = mapper.toResponse(new WebApplicationException("Bad request", 400));

    assertEquals(400, response.getStatus());
    assertNotNull(response.getEntity());
    assertTrue(response.getEntity().toString().contains("Bad request"));
  }

  @Test
  void buildsGenericExceptionPayload() throws Exception {
    ApiExceptionMapper mapper = new ApiExceptionMapper();
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
