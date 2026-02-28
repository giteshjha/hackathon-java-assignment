package com.fulfilment.application.monolith.warehouses.adapters.restapi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import jakarta.ws.rs.ext.ParamConverter;
import java.lang.annotation.Annotation;
import java.math.BigInteger;
import org.junit.jupiter.api.Test;

class LenientBigIntegerParamConverterProviderTest {

  @Test
  void shouldConvertNullAndNumbersLeniently() {
    LenientBigIntegerParamConverterProvider provider = new LenientBigIntegerParamConverterProvider();
    ParamConverter<BigInteger> converter =
        provider.getConverter(BigInteger.class, BigInteger.class, new Annotation[0]);

    assertNotNull(converter);
    assertNull(converter.fromString(null));
    assertNull(converter.fromString(""));
    assertNull(converter.fromString("null"));
    assertEquals(new BigInteger("42"), converter.fromString("42"));
    assertEquals("42", converter.toString(new BigInteger("42")));
    assertNull(converter.toString(null));
    assertNull(provider.getConverter(String.class, String.class, new Annotation[0]));
  }
}
