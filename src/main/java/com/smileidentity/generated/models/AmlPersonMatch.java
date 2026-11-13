package com.smileidentity.generated.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;

/** A single sanctions, PEP or adverse-media hit in a One-Time AML result. */
public final class AmlPersonMatch {

  @JsonProperty("name")
  private String name;

  @JsonProperty("addresses")
  private List<Map<String, Object>> addresses;

  @JsonProperty("news_summary")
  private List<NewsSummary> newsSummary;

  @JsonProperty("aliases")
  private List<Map<String, Object>> aliases;

  @JsonProperty("associations")
  private List<Map<String, Object>> associations;

  @JsonProperty("dates_of_birth")
  private List<Map<String, Object>> datesOfBirth;

  @JsonProperty("enforcement_actions")
  private List<Map<String, Object>> enforcementActions;

  @JsonProperty("nationalities")
  private List<Map<String, Object>> nationalities;

  @JsonProperty("sanctions")
  private List<Map<String, Object>> sanctions;

  @JsonProperty("pep")
  private Pep pep;

  public String getName() {
    return name;
  }

  public List<Map<String, Object>> getAddresses() {
    return addresses;
  }

  public List<NewsSummary> getNewsSummary() {
    return newsSummary;
  }

  public List<Map<String, Object>> getAliases() {
    return aliases;
  }

  public List<Map<String, Object>> getAssociations() {
    return associations;
  }

  public List<Map<String, Object>> getDatesOfBirth() {
    return datesOfBirth;
  }

  public List<Map<String, Object>> getEnforcementActions() {
    return enforcementActions;
  }

  public List<Map<String, Object>> getNationalities() {
    return nationalities;
  }

  public List<Map<String, Object>> getSanctions() {
    return sanctions;
  }

  public Pep getPep() {
    return pep;
  }

  /** Per-category summary of the person's news-media exposure. */
  public static final class NewsSummary {

    @JsonProperty("news_category")
    private String newsCategory;

    @JsonProperty("news_category_label")
    private String newsCategoryLabel;

    @JsonProperty("number_of_articles")
    private Integer numberOfArticles;

    public String getNewsCategory() {
      return newsCategory;
    }

    public String getNewsCategoryLabel() {
      return newsCategoryLabel;
    }

    public Integer getNumberOfArticles() {
      return numberOfArticles;
    }
  }

  /** Politically exposed person details. */
  public static final class Pep {

    @JsonProperty("pep_level")
    private Integer pepLevel;

    @JsonProperty("political_positions")
    private List<String> politicalPositions;

    @JsonProperty("sources")
    private List<Map<String, Object>> sources;

    public Integer getPepLevel() {
      return pepLevel;
    }

    public List<String> getPoliticalPositions() {
      return politicalPositions;
    }

    public List<Map<String, Object>> getSources() {
      return sources;
    }
  }
}
