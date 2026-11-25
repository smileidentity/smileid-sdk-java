package com.smileidentity.generated.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/** The 200 response of GET /v3/smile_links: one page of links, newest first, and paging data. */
public final class SmileLinkListResponse {

  @JsonProperty("links")
  private List<SmileLinkListItem> links;

  @JsonProperty("meta")
  private Meta meta;

  public List<SmileLinkListItem> getLinks() {
    return links;
  }

  public Meta getMeta() {
    return meta;
  }

  /** Paging data for the listing. */
  public static final class Meta {

    @JsonProperty("current_page")
    private Integer currentPage;

    @JsonProperty("limit")
    private Integer limit;

    @JsonProperty("total_count")
    private Integer totalCount;

    @JsonProperty("total_pages")
    private Integer totalPages;

    public Integer getCurrentPage() {
      return currentPage;
    }

    public Integer getLimit() {
      return limit;
    }

    public Integer getTotalCount() {
      return totalCount;
    }

    public Integer getTotalPages() {
      return totalPages;
    }
  }
}
