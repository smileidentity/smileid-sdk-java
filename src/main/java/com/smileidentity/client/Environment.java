package com.smileidentity.client;

/** Target environment. Sandbox is the default (spec §2.1). */
public enum Environment {
  SANDBOX("https://testapi.smileidentity.com"),
  PRODUCTION("https://api.smileidentity.com");

  private final String baseUrl;

  Environment(String baseUrl) {
    this.baseUrl = baseUrl;
  }

  public String baseUrl() {
    return baseUrl;
  }
}
