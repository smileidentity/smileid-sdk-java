package com.smileidentity.client;

import com.smileidentity.helpers.Validators;
import java.time.Duration;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;

/**
 * The Smile ID client (spec §2.1). Construct with {@code SmileID.builder()} and reach every
 * operation through a resource namespace, e.g. {@code smile.enhancedKyc().verify(...)} or {@code
 * smile.services().bankCodes()}.
 */
public final class SmileID {

  private final Transport transport;
  private final EnhancedKycResource enhancedKyc;
  private final DocumentsResource documents;
  private final BiometricKycResource biometricKyc;
  private final BiometricResource biometric;
  private final VerificationsResource verifications;
  private final UsersResource users;
  private final ServicesResource services;

  private SmileID(Transport transport) {
    this.transport = transport;
    this.enhancedKyc = new EnhancedKycResource(transport);
    this.documents = new DocumentsResource(transport);
    this.biometricKyc = new BiometricKycResource(transport);
    this.biometric = new BiometricResource(transport);
    this.verifications = new VerificationsResource(transport);
    this.users = new UsersResource(transport);
    this.services = new ServicesResource(transport);
  }

  public static Builder builder() {
    return new Builder();
  }

  /** POST /v3/enhanced_kyc. */
  public EnhancedKycResource enhancedKyc() {
    return enhancedKyc;
  }

  /** Document verification endpoints. */
  public DocumentsResource documents() {
    return documents;
  }

  /** POST /v3/biometric_kyc. */
  public BiometricKycResource biometricKyc() {
    return biometricKyc;
  }

  /** Biometric enrollment, authentication and compare. */
  public BiometricResource biometric() {
    return biometric;
  }

  /** Job status, polling and callback replay. */
  public VerificationsResource verifications() {
    return verifications;
  }

  /** Fraud reporting. */
  public UsersResource users() {
    return users;
  }

  /** The /v3/services endpoints. */
  public ServicesResource services() {
    return services;
  }

  // Internal: exposed for tests in this package.
  Transport transport() {
    return transport;
  }

  public static final class Builder {
    private String partnerId;
    private String apiKey;
    private Environment environment = Environment.SANDBOX;
    private String defaultCallbackUrl;
    private String baseUrl;
    private Duration timeout = Duration.ofSeconds(30);
    private int maxRetries = 2;
    private OkHttpClient httpClient;

    /** Required. Numeric string with no leading zeros. */
    public Builder partnerId(String partnerId) {
      this.partnerId = partnerId;
      return this;
    }

    /** Required. Partner API key. */
    public Builder apiKey(String apiKey) {
      this.apiKey = apiKey;
      return this;
    }

    /** Sandbox by default. */
    public Builder environment(Environment environment) {
      this.environment = environment;
      return this;
    }

    /** Used when a call omits callback_url. */
    public Builder defaultCallbackUrl(String defaultCallbackUrl) {
      this.defaultCallbackUrl = defaultCallbackUrl;
      return this;
    }

    /** Explicit base URL override; wins over environment. */
    public Builder baseUrl(String baseUrl) {
      this.baseUrl = baseUrl;
      return this;
    }

    /** Per-request total timeout. Default 30 seconds. */
    public Builder timeout(Duration timeout) {
      this.timeout = timeout;
      return this;
    }

    /** Retries for idempotent operations only (spec §2.6). Default 2. */
    public Builder maxRetries(int maxRetries) {
      this.maxRetries = maxRetries;
      return this;
    }

    /** Injectable HTTP client for testing or proxying. */
    public Builder httpClient(OkHttpClient httpClient) {
      this.httpClient = httpClient;
      return this;
    }

    public SmileID build() {
      if (partnerId == null || !partnerId.matches("^[1-9]\\d*$")) {
        throw new IllegalArgumentException(
            "partnerId is required and must be a numeric string with no leading zeros");
      }
      if (apiKey == null || apiKey.isEmpty()) {
        throw new IllegalArgumentException("apiKey is required");
      }
      if (maxRetries < 0) {
        throw new IllegalArgumentException("maxRetries must be >= 0");
      }
      if (timeout == null || timeout.isNegative() || timeout.isZero()) {
        throw new IllegalArgumentException("timeout must be positive");
      }
      String resolvedBaseUrl = validateBaseUrl(baseUrl != null ? baseUrl : environment.baseUrl());
      Validators.requireHttpsCallbackUrl(defaultCallbackUrl, "defaultCallbackUrl");
      OkHttpClient base = httpClient != null ? httpClient : new OkHttpClient();
      // retryOnConnectionFailure(false): OkHttp must not transparently re-send requests —
      // §2.6 forbids any auto-retry of non-idempotent POSTs; the transport owns all retries.
      OkHttpClient configured =
          base.newBuilder().callTimeout(timeout).retryOnConnectionFailure(false).build();
      Transport transport =
          new Transport(
              configured, resolvedBaseUrl, partnerId, apiKey, defaultCallbackUrl, maxRetries);
      return new SmileID(transport);
    }

    /**
     * base_url must be an absolute https URL with no query or fragment (fleet standard,
     * 2026-07-03). Deliberately stricter than spec §2.1; no insecure escape hatch.
     */
    private static String validateBaseUrl(String value) {
      HttpUrl parsed = value == null ? null : HttpUrl.parse(value);
      if (parsed == null) {
        throw new IllegalArgumentException("baseUrl must be an absolute https URL");
      }
      if (!"https".equals(parsed.scheme())) {
        throw new IllegalArgumentException("baseUrl must use https");
      }
      if (parsed.query() != null || parsed.fragment() != null) {
        throw new IllegalArgumentException("baseUrl must not include a query or fragment");
      }
      return value;
    }
  }
}
