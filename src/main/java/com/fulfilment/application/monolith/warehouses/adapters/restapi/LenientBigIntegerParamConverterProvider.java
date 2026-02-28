package com.fulfilment.application.monolith.warehouses.adapters.restapi;

import jakarta.ws.rs.ext.ParamConverter;
import jakarta.ws.rs.ext.ParamConverterProvider;
import jakarta.ws.rs.ext.Provider;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.math.BigInteger;

@Provider
public class LenientBigIntegerParamConverterProvider implements ParamConverterProvider {

  private static final ParamConverter<BigInteger> CONVERTER =
      new ParamConverter<>() {
        @Override
        public BigInteger fromString(String value) {
          if (value == null) {
            return null;
          }
          String normalized = value.trim();
          if (normalized.isEmpty() || "null".equalsIgnoreCase(normalized)) {
            return null;
          }
          return new BigInteger(normalized);
        }

        @Override
        public String toString(BigInteger value) {
          return value == null ? null : value.toString();
        }
      };

  @Override
  @SuppressWarnings("unchecked")
  public <T> ParamConverter<T> getConverter(
      Class<T> rawType, Type genericType, Annotation[] annotations) {
    if (BigInteger.class.equals(rawType)) {
      return (ParamConverter<T>) CONVERTER;
    }
    return null;
  }
}
