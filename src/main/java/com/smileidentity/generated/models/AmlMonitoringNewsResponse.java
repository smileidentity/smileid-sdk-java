package com.smileidentity.generated.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/** Response from GET /v3/aml/monitoring/{job_id}/news/{reference}. */
public final class AmlMonitoringNewsResponse {

  @JsonProperty("job_id")
  private String jobId;

  @JsonProperty("product")
  private String product;

  @JsonProperty("user_id")
  private String userId;

  @JsonProperty("meta")
  private Meta meta;

  @JsonProperty("news")
  private List<Item> news;

  public String getJobId() {
    return jobId;
  }

  public String getProduct() {
    return product;
  }

  public String getUserId() {
    return userId;
  }

  public Meta getMeta() {
    return meta;
  }

  public List<Item> getNews() {
    return news;
  }

  /** Pagination details. */
  public static final class Meta {

    @JsonProperty("current_page")
    private Integer currentPage;

    @JsonProperty("limit")
    private Integer limit;

    @JsonProperty("total_count")
    private Integer totalCount;

    public Integer getCurrentPage() {
      return currentPage;
    }

    public Integer getLimit() {
      return limit;
    }

    public Integer getTotalCount() {
      return totalCount;
    }
  }

  /** One news media item. */
  public static final class Item {

    @JsonProperty("body")
    private String body;

    @JsonProperty("published_date")
    private String publishedDate;

    @JsonProperty("publisher")
    private String publisher;

    @JsonProperty("publisher_country_code")
    private String publisherCountryCode;

    @JsonProperty("title")
    private String title;

    @JsonProperty("url")
    private String url;

    public String getBody() {
      return body;
    }

    public String getPublishedDate() {
      return publishedDate;
    }

    public String getPublisher() {
      return publisher;
    }

    public String getPublisherCountryCode() {
      return publisherCountryCode;
    }

    public String getTitle() {
      return title;
    }

    public String getUrl() {
      return url;
    }
  }
}
