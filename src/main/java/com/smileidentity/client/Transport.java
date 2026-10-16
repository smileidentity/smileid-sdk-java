package com.smileidentity.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smileidentity.Version;
import com.smileidentity.errors.ConnectionException;
import com.smileidentity.errors.ErrorParser;
import com.smileidentity.errors.UnexpectedResponseException;
import com.smileidentity.errors.ValidationException;
import com.smileidentity.generated.models.TokenResponse;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.Buffer;

/**
 * The single transport (spec §2.2). Builds the URL, attaches auth and telemetry headers, serializes
 * the body, mints a token per authenticated request, sends with the §2.6 retry policy, and parses
 * the response or raises a typed error.
 */
public final class Transport {

  private static final MediaType JSON_TYPE = MediaType.get("application/json");
  private static final String USER_AGENT =
      "smileid-sdk-java/"
          + Version.VERSION
          + " (java/"
          + System.getProperty("java.version", "unknown")
          + ")";

  // Mirrors the API authorizer: a token bound to a product is accepted only on that product's
  // endpoint. Paths absent here mint a token with no claims.
  private static final Map<String, String> PRODUCT_BY_PATH = productByPath();

  private final OkHttpClient http;
  private final String baseUrl;
  private final String partnerId;
  private final String apiKey;
  private final String defaultCallbackUrl;
  private final int maxRetries;
  private final ObjectMapper mapper = Json.mapper();

  private Sleeper sleeper = Sleeper.DEFAULT;

  Transport(
      OkHttpClient http,
      String baseUrl,
      String partnerId,
      String apiKey,
      String defaultCallbackUrl,
      int maxRetries) {
    this.http = http;
    this.baseUrl = trimTrailingSlash(baseUrl);
    this.partnerId = partnerId;
    this.apiKey = apiKey;
    this.defaultCallbackUrl = defaultCallbackUrl;
    this.maxRetries = maxRetries;
  }

  /** Partner id from client config (sent as SmileID-Partner-ID where required). */
  public String partnerId() {
    return partnerId;
  }

  /** Default callback URL from client config, or null (spec §2.1). */
  public String defaultCallbackUrl() {
    return defaultCallbackUrl;
  }

  // Test hook: swap sleep so backoff is instantaneous in unit tests.
  void setSleeper(Sleeper sleeper) {
    this.sleeper = sleeper;
  }

  public <T> T execute(ApiRequest req, Class<T> responseType) {
    return execute(req, responseType, false);
  }

  /**
   * Executes a request; {@code useEnrolledImage} grants the job's token a captureless Biometric KYC
   * and is never sent in the job body.
   */
  <T> T execute(ApiRequest req, Class<T> responseType, boolean useEnrolledImage) {
    byte[] bodyBytes = null;
    MediaType bodyType = null;
    switch (req.getBodyKind()) {
      case MULTIPART:
        MultipartBody multipart = buildMultipart(req);
        try {
          Buffer buffer = new Buffer();
          multipart.writeTo(buffer);
          bodyBytes = buffer.readByteArray();
        } catch (IOException e) {
          throw new ConnectionException("Could not read a binary input: " + e.getMessage(), e);
        }
        bodyType = multipart.contentType();
        break;
      case JSON:
        try {
          bodyBytes = mapper.writeValueAsBytes(req.getJsonBody());
        } catch (IOException e) {
          throw new ConnectionException("Could not serialize request body: " + e.getMessage(), e);
        }
        bodyType = JSON_TYPE;
        break;
      case NONE:
      default:
        if (!"GET".equals(req.getMethod())) {
          bodyBytes = new byte[0];
        }
        break;
    }
    return send(req, bodyBytes, bodyType, responseType, useEnrolledImage);
  }

  private <T> T send(
      ApiRequest req,
      byte[] bodyBytes,
      MediaType bodyType,
      Class<T> responseType,
      boolean useEnrolledImage) {
    HttpUrl url = buildUrl(req);
    OkHttpClient client = clientFor(req);
    String token = req.isAuthenticated() ? mintToken(req, useEnrolledImage) : null;
    int retries = 0;
    while (true) {
      Request request = buildHttpRequest(req, url, bodyBytes, bodyType, token);
      int status;
      String responseBody;
      String requestId;
      String retryAfter;
      try (Response response = client.newCall(request).execute()) {
        status = response.code();
        responseBody = response.body() != null ? response.body().string() : null;
        requestId = response.header("X-Request-ID");
        retryAfter = response.header("Retry-After");
      } catch (IOException e) {
        if (req.isIdempotent() && retries < maxRetries) {
          backoff(retries, null);
          retries++;
          continue;
        }
        throw new ConnectionException("Request to " + url + " failed: " + e.getMessage(), e);
      }
      if (req.isIdempotent() && retries < maxRetries && RetryPolicy.retryableStatus(status)) {
        backoff(retries, retryAfter);
        retries++;
        continue;
      }
      return parseResponse(req, status, responseBody, requestId, responseType);
    }
  }

  private <T> T parseResponse(
      ApiRequest req, int status, String body, String requestId, Class<T> responseType) {
    if (status >= 200 && status < 300 || (status == 404 && req.isNotFoundReturnsBody())) {
      // A success-path body must be a JSON object (fleet standard, 2026-07-03).
      try {
        JsonNode node = body == null || body.isEmpty() ? null : mapper.readTree(body);
        if (node == null || !node.isObject()) {
          throw new UnexpectedResponseException(
              "Expected a JSON object response body but got: "
                  + (body == null || body.isEmpty() ? "an empty body" : "a non-object body"),
              status,
              requestId,
              body);
        }
        return mapper.treeToValue(node, responseType);
      } catch (IOException e) {
        throw new UnexpectedResponseException(
            "Expected a JSON object response body: " + e.getMessage(), status, requestId, body);
      }
    }
    throw ErrorParser.parse(status, body, requestId);
  }

  private Request buildHttpRequest(
      ApiRequest req, HttpUrl url, byte[] bodyBytes, MediaType bodyType, String token) {
    Request.Builder rb = new Request.Builder().url(url);
    // Telemetry headers on every request, authenticated or not (§2.4).
    rb.header("SmileID-Source-SDK", "java");
    rb.header("SmileID-Source-SDK-Version", Version.VERSION);
    rb.header("User-Agent", USER_AGENT);
    for (Map.Entry<String, String> h : req.getHeaders().entrySet()) {
      rb.header(h.getKey(), h.getValue());
    }
    if (token != null) {
      rb.header("SmileID-Token", token);
    }
    if (req.isPartnerIdHeader()) {
      rb.header("SmileID-Partner-ID", partnerId);
    }
    if (req.getUserIdHeader() != null) {
      rb.header("User-ID", req.getUserIdHeader());
    }
    RequestBody body = bodyBytes == null ? null : RequestBody.create(bodyBytes, bodyType);
    rb.method(req.getMethod(), body);
    return rb.build();
  }

  private MultipartBody buildMultipart(ApiRequest req) {
    MultipartBody.Builder mb = new MultipartBody.Builder().setType(MultipartBody.FORM);
    for (Part part : req.getParts()) {
      switch (part.getKind()) {
        case TEXT:
          mb.addFormDataPart(part.getName(), part.getTextValue());
          break;
        case JSON:
          try {
            mb.addPart(
                MultipartBody.Part.createFormData(
                    part.getName(),
                    null,
                    RequestBody.create(mapper.writeValueAsBytes(part.getJsonValue()), JSON_TYPE)));
          } catch (IOException e) {
            throw new ConnectionException(
                "Could not serialize part " + part.getName() + ": " + e.getMessage(), e);
          }
          break;
        case BINARY:
        default:
          byte[] bytes;
          try {
            bytes = part.getBinary().readBytes();
          } catch (IOException e) {
            throw new ConnectionException(
                "Could not read binary input for part " + part.getName() + ": " + e.getMessage(),
                e);
          }
          String filename =
              part.getBinary().getFilename() != null
                  ? part.getBinary().getFilename()
                  : part.getDefaultFilename();
          String contentType =
              part.getBinary().getContentType() != null
                  ? part.getBinary().getContentType()
                  : part.getDefaultContentType();
          MediaType mediaType;
          try {
            mediaType = MediaType.get(contentType);
          } catch (IllegalArgumentException e) {
            // Header-injection hardening: a caller-supplied content type that is not a valid
            // media type (e.g. contains CR/LF) must never reach the wire.
            throw new ValidationException(
                "Invalid content type for part " + part.getName() + ": " + e.getMessage());
          }
          mb.addFormDataPart(part.getName(), filename, RequestBody.create(bytes, mediaType));
          break;
      }
    }
    return mb.build();
  }

  private HttpUrl buildUrl(ApiRequest req) {
    HttpUrl base = HttpUrl.get(baseUrl);
    HttpUrl.Builder ub = base.newBuilder();
    // Paths arrive pre-encoded: operations percent-encode path params as single segments
    // (fleet standard, 2026-07-03), so segments must not be encoded a second time here.
    for (String segment : req.getPath().split("/")) {
      if (!segment.isEmpty()) {
        ub.addEncodedPathSegment(segment);
      }
    }
    for (Map.Entry<String, String> q : req.getQuery().entrySet()) {
      ub.addQueryParameter(q.getKey(), q.getValue());
    }
    return ub.build();
  }

  private OkHttpClient clientFor(ApiRequest req) {
    Duration timeout = req.getOptions().getTimeout();
    if (timeout == null) {
      return http;
    }
    return http.newBuilder().callTimeout(timeout).build();
  }

  private void backoff(int attempt, String retryAfterHeader) {
    try {
      sleeper.sleep(RetryPolicy.delayMillis(attempt, retryAfterHeader));
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      InterruptedIOException io = new InterruptedIOException("Interrupted during retry backoff");
      throw new ConnectionException(io.getMessage(), io);
    }
  }

  /**
   * POST /v3/token with lowercase headers (spec §2.3/§6.0), minting a token bound to this one
   * request's user, product and grant. The body carries only the claims that are set. Retryable and
   * subject to the job's request options.
   */
  private String mintToken(ApiRequest req, boolean useEnrolledImage) {
    String product = PRODUCT_BY_PATH.get(req.getPath());
    String userId = product == null ? null : jobUserId(req);
    if (useEnrolledImage && userId == null) {
      throw new ValidationException("user_id is required when use_enrolled_image is true");
    }
    List<Part> claims = new ArrayList<>();
    if (userId != null) {
      claims.add(Part.text("user_id", userId));
    }
    if (product != null) {
      claims.add(Part.text("product", product));
    }
    if (useEnrolledImage) {
      claims.add(Part.text("use_enrolled_image", "true"));
    }
    ApiRequest.Builder tokenRequest =
        ApiRequest.builder()
            .method("POST")
            .path("/v3/token")
            .header("smileid-partner-id", partnerId)
            .header("smileid-api-key", apiKey)
            .idempotent(true)
            .options(req.getOptions());
    if (!claims.isEmpty()) {
      tokenRequest.multipart(claims);
    }
    String token = execute(tokenRequest.build(), TokenResponse.class).getToken();
    if (token == null || token.isEmpty()) {
      throw new UnexpectedResponseException(
          "Token response did not contain a token", 200, null, null);
    }
    return token;
  }

  // The API trims the User-ID header but not the token's user_id claim, so a padded id would mint
  // a token for a different user than the job names.
  private static String jobUserId(ApiRequest req) {
    String userId = req.getUserIdHeader();
    if (userId == null) {
      for (Part part : req.getParts()) {
        if (part.getKind() == Part.Kind.TEXT && "user_id".equals(part.getName())) {
          userId = part.getTextValue();
          break;
        }
      }
    }
    if (userId != null && (userId.isEmpty() || !userId.equals(userId.trim()))) {
      throw new ValidationException("user_id must not be empty or padded with whitespace");
    }
    return userId;
  }

  private static Map<String, String> productByPath() {
    Map<String, String> m = new HashMap<>();
    m.put("/v3/enhanced_kyc", "enhanced_kyc");
    m.put("/v3/document_verification", "document_verification");
    m.put("/v3/enhanced_document_verification", "enhanced_document_verification");
    m.put("/v3/residency_document_verification", "residency_document_verification");
    m.put("/v3/biometric_kyc", "biometric_kyc");
    m.put("/v3/registration", "smart_selfie_registration");
    m.put("/v3/authentication", "smart_selfie_authentication");
    m.put("/v3/compare", "smart_selfie_compare");
    return m;
  }

  private static String trimTrailingSlash(String url) {
    String result = url;
    while (result.endsWith("/")) {
      result = result.substring(0, result.length() - 1);
    }
    return result;
  }
}
