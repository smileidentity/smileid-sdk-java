package com.smileidentity.generated.models;

/** Query parameters for GET /v3/smile_links. All are optional. */
public final class SmileLinkListParams {

  private final Integer limit;
  private final Integer page;
  private final Boolean singleUse;

  private SmileLinkListParams(Builder b) {
    this.limit = b.limit;
    this.page = b.page;
    this.singleUse = b.singleUse;
  }

  public static Builder builder() {
    return new Builder();
  }

  public Integer getLimit() {
    return limit;
  }

  public Integer getPage() {
    return page;
  }

  public Boolean getSingleUse() {
    return singleUse;
  }

  public static final class Builder {
    private Integer limit;
    private Integer page;
    private Boolean singleUse;

    /** Page size, 1 to 100. The API defaults to 20. */
    public Builder limit(Integer limit) {
      this.limit = limit;
      return this;
    }

    /** Page number, 1-indexed. The API defaults to 1. */
    public Builder page(Integer page) {
      this.page = page;
      return this;
    }

    /** True returns only single-use links, false only reusable ones. Omit for both. */
    public Builder singleUse(Boolean singleUse) {
      this.singleUse = singleUse;
      return this;
    }

    public SmileLinkListParams build() {
      return new SmileLinkListParams(this);
    }
  }
}
