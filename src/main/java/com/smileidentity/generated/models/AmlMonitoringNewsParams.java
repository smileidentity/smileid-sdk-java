package com.smileidentity.generated.models;

/** Query parameters for GET /v3/aml/monitoring/{job_id}/news/{reference}. */
public final class AmlMonitoringNewsParams {

  private final String newsCategory;
  private final Integer page;
  private final Integer limit;

  private AmlMonitoringNewsParams(Builder b) {
    this.newsCategory = b.newsCategory;
    this.page = b.page;
    this.limit = b.limit;
  }

  public static Builder builder() {
    return new Builder();
  }

  public String getNewsCategory() {
    return newsCategory;
  }

  public Integer getPage() {
    return page;
  }

  public Integer getLimit() {
    return limit;
  }

  public static final class Builder {
    private String newsCategory;
    private Integer page;
    private Integer limit;

    /** Required. One of the spec's news categories, such as {@code financial_crime}. */
    public Builder newsCategory(String newsCategory) {
      this.newsCategory = newsCategory;
      return this;
    }

    /** Results page, 1 to 100; the server defaults to 1. */
    public Builder page(Integer page) {
      this.page = page;
      return this;
    }

    /** Items per page, 1 to 100; the server defaults to 25. */
    public Builder limit(Integer limit) {
      this.limit = limit;
      return this;
    }

    public AmlMonitoringNewsParams build() {
      return new AmlMonitoringNewsParams(this);
    }
  }
}
