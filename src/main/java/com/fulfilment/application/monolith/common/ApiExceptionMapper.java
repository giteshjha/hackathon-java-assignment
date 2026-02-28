package com.fulfilment.application.monolith.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.inject.Inject;
import jakarta.persistence.OptimisticLockException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.jboss.logging.Logger;

@Provider
public class ApiExceptionMapper implements ExceptionMapper<Exception> {

  private static final Logger LOGGER = Logger.getLogger(ApiExceptionMapper.class);

  @Inject ObjectMapper objectMapper;

  @Override
  public Response toResponse(Exception exception) {
    LOGGER.error("Failed to handle request", exception);

    int code = 500;
    if (exception instanceof WebApplicationException webApplicationException) {
      code = webApplicationException.getResponse().getStatus();
    } else if (isCausedByOptimisticLock(exception)) {
      code = 409;
    }

    ObjectNode exceptionJson = objectMapper.createObjectNode();
    exceptionJson.put("exceptionType", exception.getClass().getName());
    exceptionJson.put("code", code);
    String message = code == 409
        ? "The resource was modified by another request. Please reload and try again."
        : exception.getMessage();
    if (message != null) {
      exceptionJson.put("error", message);
    }

    return Response.status(code).entity(exceptionJson).build();
  }

  /** Walks the cause chain to detect an optimistic locking failure. */
  private boolean isCausedByOptimisticLock(Throwable t) {
    for (Throwable cause = t; cause != null; cause = cause.getCause()) {
      if (cause instanceof OptimisticLockException) {
        return true;
      }
    }
    return false;
  }
}
