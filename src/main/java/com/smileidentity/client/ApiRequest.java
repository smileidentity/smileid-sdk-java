package com.smileidentity.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** An operation request routed through the transport (spec §2A build_request). */
public final class ApiRequest {

  public enum BodyKind {
    NONE,
    MULTIPART,
    JSON
  }

  private final String method;
  private final String path;
  private final Map<String, String> query;
  private final Map<String, String> headers;
  private final boolean authenticated;
  private final boolean partnerIdHeader;
  private final String userIdHeader;
  private final BodyKind bodyKind;
  private final List<Part> parts;
  private final Object jsonBody;
  private final boolean idempotent;
  private final boolean notFoundReturnsBody;
  private final boolean jobIdFromHeader;
  private final Map<String, String> headerFields;
  private final Map<String, String> headerOnlyFields;
  private final RequestOptions options;

  private ApiRequest(Builder b) {
    this.method = b.method;
    this.path = b.path;
    this.query = Collections.unmodifiableMap(b.query);
    this.headers = Collections.unmodifiableMap(b.headers);
    this.authenticated = b.authenticated;
    this.partnerIdHeader = b.partnerIdHeader;
    this.userIdHeader = b.userIdHeader;
    this.bodyKind = b.bodyKind;
    this.parts = Collections.unmodifiableList(b.parts);
    this.jsonBody = b.jsonBody;
    this.idempotent = b.idempotent;
    this.notFoundReturnsBody = b.notFoundReturnsBody;
    this.jobIdFromHeader = b.jobIdFromHeader;
    this.headerFields = Collections.unmodifiableMap(b.headerFields);
    this.headerOnlyFields = Collections.unmodifiableMap(b.headerOnlyFields);
    this.options = b.options == null ? RequestOptions.none() : b.options;
  }

  public static Builder builder() {
    return new Builder();
  }

  public String getMethod() {
    return method;
  }

  public String getPath() {
    return path;
  }

  public Map<String, String> getQuery() {
    return query;
  }

  public Map<String, String> getHeaders() {
    return headers;
  }

  public boolean isAuthenticated() {
    return authenticated;
  }

  public boolean isPartnerIdHeader() {
    return partnerIdHeader;
  }

  public String getUserIdHeader() {
    return userIdHeader;
  }

  public BodyKind getBodyKind() {
    return bodyKind;
  }

  public List<Part> getParts() {
    return parts;
  }

  public Object getJsonBody() {
    return jsonBody;
  }

  public boolean isIdempotent() {
    return idempotent;
  }

  public boolean isNotFoundReturnsBody() {
    return notFoundReturnsBody;
  }

  /**
   * True when a success body's {@code job_id} is taken from the {@code SmileID-Job-ID} response
   * header (sync endpoints, whose result body carries no job id).
   */
  public boolean isJobIdFromHeader() {
    return jobIdFromHeader;
  }

  /**
   * Response headers copied into a success body, keyed by header name with the body field as the
   * value. A header that is present wins over the body's field; an absent one leaves it alone.
   */
  public Map<String, String> getHeaderFields() {
    return headerFields;
  }

  /**
   * Response headers copied into a success body, keyed by header name with the body field as the
   * value. The field is always the header's value, and null when the header is absent.
   */
  public Map<String, String> getHeaderOnlyFields() {
    return headerOnlyFields;
  }

  public RequestOptions getOptions() {
    return options;
  }

  public static final class Builder {
    private String method = "GET";
    private String path;
    private final Map<String, String> query = new LinkedHashMap<>();
    private final Map<String, String> headers = new LinkedHashMap<>();
    private boolean authenticated;
    private boolean partnerIdHeader;
    private String userIdHeader;
    private BodyKind bodyKind = BodyKind.NONE;
    private final List<Part> parts = new ArrayList<>();
    private Object jsonBody;
    private boolean idempotent;
    private boolean notFoundReturnsBody;
    private boolean jobIdFromHeader;
    private final Map<String, String> headerFields = new LinkedHashMap<>();
    private final Map<String, String> headerOnlyFields = new LinkedHashMap<>();
    private RequestOptions options;

    public Builder method(String method) {
      this.method = method;
      return this;
    }

    public Builder path(String path) {
      this.path = path;
      return this;
    }

    public Builder query(String name, String value) {
      this.query.put(name, value);
      return this;
    }

    public Builder header(String name, String value) {
      this.headers.put(name, value);
      return this;
    }

    public Builder authenticated(boolean authenticated) {
      this.authenticated = authenticated;
      return this;
    }

    public Builder partnerIdHeader(boolean partnerIdHeader) {
      this.partnerIdHeader = partnerIdHeader;
      return this;
    }

    public Builder userIdHeader(String userIdHeader) {
      this.userIdHeader = userIdHeader;
      return this;
    }

    public Builder multipart(List<Part> parts) {
      this.bodyKind = BodyKind.MULTIPART;
      this.parts.addAll(parts);
      return this;
    }

    public Builder jsonBody(Object jsonBody) {
      this.bodyKind = BodyKind.JSON;
      this.jsonBody = jsonBody;
      return this;
    }

    public Builder idempotent(boolean idempotent) {
      this.idempotent = idempotent;
      return this;
    }

    public Builder notFoundReturnsBody(boolean notFoundReturnsBody) {
      this.notFoundReturnsBody = notFoundReturnsBody;
      return this;
    }

    /**
     * Sets {@code job_id} on a success body from the {@code SmileID-Job-ID} response header, when
     * the response has one. The header wins over any {@code job_id} in the body.
     */
    public Builder jobIdFromHeader(boolean jobIdFromHeader) {
      this.jobIdFromHeader = jobIdFromHeader;
      return this;
    }

    /**
     * Sets {@code field} on a success body from the {@code header} response header, when the
     * response has one. The header wins over the body's field; without it the body's field is kept.
     * Header names are matched case-insensitively, and a blank header counts as absent.
     */
    public Builder headerField(String header, String field) {
      this.headerFields.put(header, field);
      return this;
    }

    /**
     * Sets {@code field} on a success body to the {@code header} response header's value, or to
     * null when the response has no such header, whatever the body says. Header names are matched
     * case-insensitively, and a blank header counts as absent.
     */
    public Builder headerOnlyField(String header, String field) {
      this.headerOnlyFields.put(header, field);
      return this;
    }

    public Builder options(RequestOptions options) {
      this.options = options;
      return this;
    }

    public ApiRequest build() {
      return new ApiRequest(this);
    }
  }
}
