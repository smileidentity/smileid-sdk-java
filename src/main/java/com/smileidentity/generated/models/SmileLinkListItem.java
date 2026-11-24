package com.smileidentity.generated.models;

import com.fasterxml.jackson.annotation.JsonProperty;

/** One link in a listing: the link resource plus the outcome counts of the jobs run against it. */
public final class SmileLinkListItem extends SmileLinkResource {

  @JsonProperty("clear")
  private Integer clear;

  @JsonProperty("block")
  private Integer block;

  @JsonProperty("attention")
  private Integer attention;

  @JsonProperty("error")
  private Integer error;

  @JsonProperty("successful_jobs")
  private Integer successfulJobs;

  @JsonProperty("failed_jobs")
  private Integer failedJobs;

  @JsonProperty("total_jobs")
  private Integer totalJobs;

  /** Jobs that resolved clear. */
  public Integer getClear() {
    return clear;
  }

  /** Jobs that resolved block. */
  public Integer getBlock() {
    return block;
  }

  /** Jobs that resolved attention. */
  public Integer getAttention() {
    return attention;
  }

  /** Jobs that ended in an error. */
  public Integer getError() {
    return error;
  }

  /** Clear plus attention. */
  public Integer getSuccessfulJobs() {
    return successfulJobs;
  }

  /** Block plus error. */
  public Integer getFailedJobs() {
    return failedJobs;
  }

  /** Clear, block, attention and error together. Jobs still processing are not counted. */
  public Integer getTotalJobs() {
    return totalJobs;
  }
}
