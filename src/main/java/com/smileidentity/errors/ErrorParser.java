package com.smileidentity.errors;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Turns an HTTP error response into a typed {@link SmileIDException} (spec §2A parse_error).
 *
 * <p>Handles both wire shapes: {@code {status, message}} (used almost everywhere, sometimes
 * reordered as {@code {message, status}}) and {@code {error, code}} (the three unauthenticated
 * services endpoints). The class is selected by HTTP status, never by body contents.
 */
public final class ErrorParser {

  private static final ObjectMapper MAPPER = new ObjectMapper();

  private ErrorParser() {}

  public static SmileIDException parse(int statusCode, String rawBody, String requestId) {
    String status = null;
    String message = null;
    String code = null;
    if (rawBody != null && !rawBody.isEmpty()) {
      try {
        JsonNode node = MAPPER.readTree(rawBody);
        if (node != null && node.isObject()) {
          message = textOrNull(node, "message");
          if (message == null) {
            message = textOrNull(node, "error");
          }
          code = textOrNull(node, "code");
          status = textOrNull(node, "status");
        }
      } catch (Exception ignored) {
        // Non-JSON body: fall back to the HTTP reason phrase below.
      }
    }
    if (message == null) {
      message = defaultReason(statusCode);
    }
    return build(statusCode, status, message, code, requestId, rawBody);
  }

  private static SmileIDException build(
      int statusCode, String status, String message, String code, String requestId, String raw) {
    Integer sc = statusCode;
    switch (statusCode) {
      case 400:
      case 415:
        return new InvalidRequestException(message, sc, status, code, requestId, raw);
      case 401:
        return new AuthenticationException(message, sc, status, code, requestId, raw);
      case 402:
        return new PaymentRequiredException(message, sc, status, code, requestId, raw);
      case 403:
        return new PermissionException(message, sc, status, code, requestId, raw);
      case 404:
        return new NotFoundException(message, sc, status, code, requestId, raw);
      case 409:
        return new ConflictException(message, sc, status, code, requestId, raw);
      case 413:
        return new PayloadTooLargeException(message, sc, status, code, requestId, raw);
      case 429:
        return new RateLimitException(message, sc, status, code, requestId, raw);
      default:
        return new ApiException(message, sc, status, code, requestId, raw);
    }
  }

  private static String textOrNull(JsonNode node, String field) {
    JsonNode v = node.get(field);
    if (v == null || v.isNull()) {
      return null;
    }
    return v.asText();
  }

  private static String defaultReason(int statusCode) {
    switch (statusCode) {
      case 400:
        return "Bad Request";
      case 401:
        return "Unauthorized";
      case 402:
        return "Payment Required";
      case 403:
        return "Forbidden";
      case 404:
        return "Not Found";
      case 408:
        return "Request Timeout";
      case 409:
        return "Conflict";
      case 413:
        return "Content Too Large";
      case 415:
        return "Unsupported Media Type";
      case 429:
        return "Too Many Requests";
      case 500:
        return "Internal Server Error";
      case 502:
        return "Bad Gateway";
      case 503:
        return "Service Unavailable";
      case 504:
        return "Gateway Timeout";
      default:
        return "HTTP " + statusCode;
    }
  }
}
