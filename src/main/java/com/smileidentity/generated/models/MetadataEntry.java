package com.smileidentity.generated.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/** One metadata item: {@code {name, value}} (spec §5.1). */
@JsonPropertyOrder({"name", "value"})
public final class MetadataEntry {

  @JsonProperty("name")
  private final String name;

  @JsonProperty("value")
  private final String value;

  public MetadataEntry(String name, String value) {
    this.name = name;
    this.value = value;
  }

  public String getName() {
    return name;
  }

  public String getValue() {
    return value;
  }
}
