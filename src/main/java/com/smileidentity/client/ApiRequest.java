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

    public Builder options(RequestOptions options) {
      this.options = options;
      return this;
    }

    public ApiRequest build() {
      return new ApiRequest(this);
    }
  }
}
