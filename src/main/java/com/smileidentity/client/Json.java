package com.smileidentity.client;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

/** The single Jackson mapper used at the wire boundary. */
public final class Json {

  private static final ObjectMapper MAPPER =
      new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

  private Json() {}

  public static ObjectMapper mapper() {
    return MAPPER;
  }
}
