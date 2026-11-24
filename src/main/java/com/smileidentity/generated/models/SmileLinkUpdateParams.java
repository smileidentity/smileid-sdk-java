package com.smileidentity.generated.models;

import java.util.List;

/**
 * Request parameters for PUT /v3/smile_links/{refId}. At least one of {@code idTypes} or {@code
 * isDisabled} is required.
 */
public final class SmileLinkUpdateParams {

  private final List<SmileLinkIdType> idTypes;
  private final Boolean isDisabled;

  private SmileLinkUpdateParams(Builder b) {
    this.idTypes = b.idTypes;
    this.isDisabled = b.isDisabled;
  }

  public static Builder builder() {
    return new Builder();
  }

  public List<SmileLinkIdType> getIdTypes() {
    return idTypes;
  }

  public Boolean getIsDisabled() {
    return isDisabled;
  }

  public static final class Builder {
    private List<SmileLinkIdType> idTypes;
    private Boolean isDisabled;

    /** Replaces the link's current id types. */
    public Builder idTypes(List<SmileLinkIdType> idTypes) {
      this.idTypes = idTypes;
      return this;
    }

    /** True disables the link; false re-enables it. */
    public Builder isDisabled(Boolean isDisabled) {
      this.isDisabled = isDisabled;
      return this;
    }

    public SmileLinkUpdateParams build() {
      return new SmileLinkUpdateParams(this);
    }
  }
}
