package com.smileidentity.example;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;
import org.junit.jupiter.api.Test;

final class ExampleAppTest {
  private static final ObjectMapper JSON = new ObjectMapper();

  @Test
  void servicesListsReferenceDataWithoutAuthentication() throws Exception {
    FakeSmileApi fake = new FakeSmileApi();
    ByteArrayOutputStream out = new ByteArrayOutputStream();

    int code =
        new ExampleApp(env(), new PrintStream(out), System.err, fake.client())
            .run(new String[] {"--base-url", "https://api.test", "services", "--country", "NG"});

    assertEquals(0, code);
    Map<String, Object> result = json(out);
    assertEquals("NG", result.get("country"));
    assertEquals(0, fake.tokenCalls);
    assertTrue(out.toString(StandardCharsets.UTF_8).contains("\"code\" : \"001\""));
    assertTrue(out.toString(StandardCharsets.UTF_8).contains("\"type\" : \"NIN\""));
  }

  @Test
  void enhancedKycSubmitsVerification() throws Exception {
    FakeSmileApi fake = new FakeSmileApi();
    ByteArrayOutputStream out = new ByteArrayOutputStream();

    int code =
        new ExampleApp(env(), new PrintStream(out), System.err, fake.client())
            .run(
                new String[] {
                  "--base-url",
                  "https://api.test",
                  "--callback-url",
                  "https://example.com/smile-callback",
                  "enhanced-kyc",
                  "--country",
                  "NG",
                  "--id-type",
                  "NIN",
                  "--id-number",
                  "12345678901",
                  "--given-names",
                  "Amina Fatou",
                  "--last-name",
                  "Clearwater",
                  "--email",
                  "amina.clearwater@example.com"
                });

    assertEquals(0, code);
    Map<String, Object> result = json(out);
    assertEquals("job_enhanced_123", result.get("job_id"));
    assertEquals(true, result.get("accepted"));
    assertEquals(1, fake.tokenCalls);
    RecordedRequest request = fake.find("/v3/enhanced_kyc");
    assertTrue(request.headers.get("SmileID-Token").get(0).startsWith("eyJ"));
    assertTrue(request.body.contains("name=\"country\""));
    assertTrue(request.body.contains("NG"));
    assertTrue(request.body.contains("name=\"id_type\""));
    assertTrue(request.body.contains("NIN"));
    assertTrue(request.body.contains("https://example.com/smile-callback"));
    assertTrue(request.body.contains("\"given_names\":\"Amina Fatou\""));
  }

  @Test
  void statusRetrievesVerification() throws Exception {
    FakeSmileApi fake = new FakeSmileApi();
    ByteArrayOutputStream out = new ByteArrayOutputStream();

    int code =
        new ExampleApp(env(), new PrintStream(out), System.err, fake.client())
            .run(
                new String[] {
                  "--base-url", "https://api.test", "status", "--job-id", "job_enhanced_123"
                });

    assertEquals(0, code);
    Map<String, Object> result = json(out);
    assertEquals("clear", result.get("status"));
    assertEquals("Job completed", result.get("message"));
  }

  @Test
  void replayRequestsCallbackReplay() throws Exception {
    FakeSmileApi fake = new FakeSmileApi();
    ByteArrayOutputStream out = new ByteArrayOutputStream();

    int code =
        new ExampleApp(env(), new PrintStream(out), System.err, fake.client())
            .run(
                new String[] {
                  "--base-url",
                  "https://api.test",
                  "replay",
                  "--job-id",
                  "job_enhanced_123",
                  "--callback-url",
                  "https://example.com/replay-callback"
                });

    assertEquals(0, code);
    Map<String, Object> result = json(out);
    assertEquals("success", result.get("status"));
    assertEquals("job_enhanced_123", result.get("job_id"));
    RecordedRequest request = fake.find("/v3/replay/job_enhanced_123");
    // Replay sends multipart/form-data with one callback_url part (spec §6.10 as corrected).
    // The Content-Type header is added after application interceptors, so assert on the body.
    assertTrue(request.body.contains("name=\"callback_url\""));
    assertTrue(request.body.contains("https://example.com/replay-callback"));
  }

  @Test
  void helpDoesNotRequireCredentials() {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    int code =
        new ExampleApp(Map.of(), new PrintStream(out), System.err, null).run(new String[] {"help"});
    assertEquals(0, code);
    assertTrue(out.toString(StandardCharsets.UTF_8).contains("Usage:"));
  }

  @Test
  void missingCredentialsReturnsUsageExit() {
    ByteArrayOutputStream err = new ByteArrayOutputStream();
    int code =
        new ExampleApp(Map.of(), System.out, new PrintStream(err), null)
            .run(new String[] {"services"});
    assertEquals(2, code);
    assertTrue(err.toString(StandardCharsets.UTF_8).contains("SMILE_PARTNER_ID"));
  }

  private static Map<String, String> env() {
    return Map.of("SMILE_PARTNER_ID", "12345", "SMILE_API_KEY", "test-api-key");
  }

  private static Map<String, Object> json(ByteArrayOutputStream out) throws Exception {
    return JSON.readValue(out.toByteArray(), new TypeReference<Map<String, Object>>() {});
  }

  private static final class FakeSmileApi implements Interceptor {
    final List<RecordedRequest> requests = new ArrayList<>();
    int tokenCalls;

    OkHttpClient client() {
      return new OkHttpClient.Builder().addInterceptor(this).build();
    }

    @Override
    public Response intercept(Chain chain) throws java.io.IOException {
      Request request = chain.request();
      String body = body(request);
      requests.add(
          new RecordedRequest(request.url().encodedPath(), request.headers().toMultimap(), body));
      String path = request.url().encodedPath();
      if (path.equals("/v3/token")) {
        tokenCalls++;
        assertEquals("12345", request.header("smileid-partner-id"));
        assertEquals("test-api-key", request.header("smileid-api-key"));
        return response(request, 200, "{\"token\":\"" + jwt() + "\"}");
      }
      if (path.equals("/v3/services/bank_codes")) {
        return response(
            request,
            200,
            "{\"bank_codes\":[{\"code\":\"001\",\"country\":\"NG\",\"name\":\"Example Bank\"}]}");
      }
      if (path.equals("/v3/services/supported_id_types")) {
        return response(
            request,
            200,
            "{\"id_types\":[{\"country\":\"NG\",\"label\":\"National Identification Number\",\"regex\":\"^\\\\d{11}$\",\"required_fields\":[\"id_number\"],\"type\":\"NIN\"}]}");
      }
      if (path.equals("/v3/services/supported_documents")) {
        return response(
            request,
            200,
            "{\"valid_documents\":[{\"country\":{\"code\":\"NG\",\"name\":\"Nigeria\",\"continent\":\"Africa\"},\"id_types\":[{\"code\":\"PASSPORT\",\"name\":\"Passport\",\"example\":[\"A12345678\"],\"has_back\":false}]}]}");
      }
      if (path.equals("/v3/enhanced_kyc")) {
        return response(
            request,
            202,
            "{\"status\":\"Accepted\",\"message\":\"submitted\",\"job_id\":\"job_enhanced_123\",\"user_id\":\"user_123\"}");
      }
      if (path.equals("/v3/status/job_enhanced_123")) {
        return response(
            request,
            200,
            "{\"status\":\"clear\",\"message\":\"Job completed\",\"job_id\":\"job_enhanced_123\",\"user_id\":\"user_123\"}");
      }
      if (path.equals("/v3/replay/job_enhanced_123")) {
        return response(
            request,
            200,
            "{\"status\":\"success\",\"message\":\"replayed\",\"job_id\":\"job_enhanced_123\",\"user_id\":\"user_123\"}");
      }
      return response(request, 404, "{\"status\":\"not_found\"}");
    }

    RecordedRequest find(String path) {
      return requests.stream()
          .filter(r -> r.path.equals(path))
          .findFirst()
          .orElseThrow(() -> new AssertionError("no request for " + path));
    }

    private Response response(Request request, int status, String body) {
      return new Response.Builder()
          .request(request)
          .protocol(Protocol.HTTP_1_1)
          .code(status)
          .message("OK")
          .body(ResponseBody.create(body, MediaType.get("application/json")))
          .build();
    }

    private String body(Request request) throws java.io.IOException {
      if (request.body() == null) return "";
      Buffer buffer = new Buffer();
      request.body().writeTo(buffer);
      return buffer.readUtf8();
    }

    private String jwt() {
      String header =
          Base64.getUrlEncoder()
              .withoutPadding()
              .encodeToString(
                  "{\"alg\":\"HS256\",\"typ\":\"JWT\"}".getBytes(StandardCharsets.UTF_8));
      String payload =
          Base64.getUrlEncoder()
              .withoutPadding()
              .encodeToString("{\"exp\":4102444800}".getBytes(StandardCharsets.UTF_8));
      return header + "." + payload + ".signature";
    }
  }

  private static final class RecordedRequest {
    final String path;
    final Map<String, List<String>> headers;
    final String body;

    RecordedRequest(String path, Map<String, List<String>> headers, String body) {
      this.path = path;
      this.headers = headers;
      this.body = body;
    }
  }
}
