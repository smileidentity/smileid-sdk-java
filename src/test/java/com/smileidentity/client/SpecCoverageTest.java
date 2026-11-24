package com.smileidentity.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;

/**
 * Keeps spec-coverage.json honest against the code. The spec-coverage CI job checks the file
 * against the spec; this test checks that every supported {@code method} exists on a client and
 * that the supported paths are exactly the request paths the SDK sends.
 *
 * <p>The SDK has no operation table: OperationProducts maps spec paths to token products and
 * includes operations the SDK doesn't call. Request paths are defined in one place, the
 * per-operation functions in {@code generated/operations}, plus the token request in {@code
 * Transport}, so the test reads them from there.
 */
class SpecCoverageTest {

  private static final Path MANIFEST = Paths.get("spec-coverage.json");
  private static final Path OPERATIONS_DIR =
      Paths.get("src/main/java/com/smileidentity/generated/operations");
  private static final Path TRANSPORT =
      Paths.get("src/main/java/com/smileidentity/client/Transport.java");

  // No public method calls POST /v3/token: every authenticated call fetches a token first.
  private static final String TOKEN_OPERATION = "getV3Token";

  private static final Pattern FIELD_REF = Pattern.compile("\\b([A-Z]\\w*)\\.(\\w+)\\(\\)");
  private static final Pattern PATH_PARAM = Pattern.compile("\\{[^}]*}");
  private static final String[] FIELD_PACKAGES = {
    "com.smileidentity.generated.models.", "com.smileidentity.errors."
  };

  private static JsonNode operations() throws IOException {
    JsonNode manifest = new ObjectMapper().readTree(MANIFEST.toFile());
    JsonNode operations = manifest.get("operations");
    assertNotNull(operations, "spec-coverage.json has no operations");
    return operations;
  }

  private static Map<String, JsonNode> supported() throws IOException {
    Map<String, JsonNode> supported = new TreeMap<>();
    for (Iterator<Map.Entry<String, JsonNode>> it = operations().fields(); it.hasNext(); ) {
      Map.Entry<String, JsonNode> entry = it.next();
      if (!entry.getValue().has("unsupported")) {
        supported.put(entry.getKey(), entry.getValue());
      }
    }
    return supported;
  }

  private static SmileID dummyClient() {
    return SmileID.builder().partnerId("1").apiKey("dummy-key").build();
  }

  @Test
  void everySupportedMethodExistsOnTheClient() throws Exception {
    Map<String, JsonNode> supported = supported();
    assertFalse(supported.isEmpty());
    SmileID client = dummyClient();
    for (Map.Entry<String, JsonNode> entry : supported.entrySet()) {
      String opId = entry.getKey();
      String method = entry.getValue().path("method").asText();
      boolean internal = TOKEN_OPERATION.equals(opId);
      assertTrue(
          method.startsWith("client."), opId + ": method must start with client.: " + method);
      String[] steps = method.substring("client.".length()).split("\\.");
      Object target = client;
      for (int i = 0; i < steps.length - 1; i++) {
        String accessor = steps[i];
        assertTrue(accessor.endsWith("()"), opId + ": " + accessor + " must be an accessor call");
        Method m =
            findMethod(target.getClass(), accessor.substring(0, accessor.length() - 2), internal);
        assertNotNull(m, opId + ": " + target.getClass().getSimpleName() + " has no " + accessor);
        assertEquals(0, m.getParameterCount(), opId + ": " + accessor + " takes arguments");
        m.setAccessible(true);
        target = m.invoke(target);
        assertNotNull(target, opId + ": " + accessor + " returned null");
      }
      String last = steps[steps.length - 1];
      assertNotNull(
          findMethod(target.getClass(), last, internal),
          opId + ": " + target.getClass().getSimpleName() + " has no method " + last);
    }
  }

  @Test
  void theTokenOperationNamesTheInternalFunctionThatCallsIt() throws Exception {
    JsonNode token = operations().get(TOKEN_OPERATION);
    assertNotNull(token);
    assertEquals("client.transport().mintToken", token.path("method").asText());
    assertEquals("POST /v3/token", token.path("path").asText());
  }

  /** Public methods only, unless internal is true (the token request has no public method). */
  private static Method findMethod(Class<?> type, String name, boolean internal) {
    for (Method m : internal ? type.getDeclaredMethods() : type.getMethods()) {
      if (m.getName().equals(name) && (internal || Modifier.isPublic(m.getModifiers()))) {
        return m;
      }
    }
    return null;
  }

  @Test
  void responseHeaderFieldsExist() throws Exception {
    for (Map.Entry<String, JsonNode> entry : supported().entrySet()) {
      JsonNode headers = entry.getValue().path("response_headers");
      for (Iterator<Map.Entry<String, JsonNode>> it = headers.fields(); it.hasNext(); ) {
        Map.Entry<String, JsonNode> header = it.next();
        if (!header.getValue().isTextual()) {
          continue;
        }
        Matcher m = FIELD_REF.matcher(header.getValue().asText());
        boolean found = false;
        while (m.find()) {
          found = true;
          Class<?> type = loadType(m.group(1));
          assertNotNull(type, entry.getKey() + ": no type " + m.group(1));
          assertNotNull(
              findMethod(type, m.group(2), false),
              entry.getKey() + ": " + m.group(1) + " has no public " + m.group(2) + "()");
        }
        assertTrue(found, entry.getKey() + ": " + header.getKey() + " names no Type.getter()");
      }
    }
  }

  private static Class<?> loadType(String simpleName) {
    for (String pkg : FIELD_PACKAGES) {
      try {
        return Class.forName(pkg + simpleName);
      } catch (ClassNotFoundException e) {
        // try the next package
      }
    }
    return null;
  }

  @Test
  void supportedPathsAreExactlyTheRequestPathsTheSdkSends() throws Exception {
    TreeSet<String> manifestPaths = new TreeSet<>();
    for (Map.Entry<String, JsonNode> entry : supported().entrySet()) {
      String path = entry.getValue().path("path").asText();
      int space = path.indexOf(' ');
      assertTrue(space > 0, entry.getKey() + ": path must be \"METHOD /v3/...\": " + path);
      manifestPaths.add(PATH_PARAM.matcher(path.substring(space + 1)).replaceAll("{}"));
    }
    // An extra path on either side is an operation the file and the code disagree about: an
    // unsupported operation has no path, so a request to it shows up here as well.
    assertEquals(manifestPaths, sdkRequestPaths());
  }

  /**
   * Every request path the SDK builds, with path parameters as {}: string literals starting with
   * /v3/ in the per-operation functions (and any concatenation that follows them), plus the token
   * request's {@code .path(...)} in Transport.
   */
  private static TreeSet<String> sdkRequestPaths() throws IOException {
    TreeSet<String> paths = new TreeSet<>();
    List<Path> files = new ArrayList<>();
    try (DirectoryStream<Path> dir = Files.newDirectoryStream(OPERATIONS_DIR, "*Operations.java")) {
      for (Path file : dir) {
        files.add(file);
      }
    }
    Collections.sort(files);
    assertFalse(files.isEmpty(), "no operation sources in " + OPERATIONS_DIR);
    for (Path file : files) {
      paths.addAll(pathsIn(read(file), "\"/v3/"));
    }
    paths.addAll(pathsIn(read(TRANSPORT), ".path(\"/v3/"));
    return paths;
  }

  private static String read(Path file) throws IOException {
    return new String(Files.readAllBytes(file), StandardCharsets.UTF_8);
  }

  /** Path expressions starting at each occurrence of marker, which ends with "/v3/. */
  static List<String> pathsIn(String source, String marker) {
    List<String> paths = new ArrayList<>();
    int from = 0;
    while (true) {
      int at = source.indexOf(marker, from);
      if (at < 0) {
        return paths;
      }
      int i = at + marker.length() - "/v3/".length() - 1; // the opening quote
      StringBuilder path = new StringBuilder();
      i = readLiteral(source, i, path);
      while (true) {
        int next = skipSpace(source, i);
        if (next >= source.length() || source.charAt(next) != '+') {
          break;
        }
        i = skipSpace(source, next + 1);
        if (source.charAt(i) == '"') {
          i = readLiteral(source, i, path);
        } else {
          i = skipExpression(source, i);
          path.append("{}");
        }
      }
      paths.add(path.toString());
      from = i;
    }
  }

  private static int readLiteral(String source, int quote, StringBuilder out) {
    int end = source.indexOf('"', quote + 1);
    out.append(source, quote + 1, end);
    return end + 1;
  }

  private static int skipSpace(String source, int i) {
    while (i < source.length() && Character.isWhitespace(source.charAt(i))) {
      i++;
    }
    return i;
  }

  /** Skips one non-literal operand of a + chain, up to a top-level +, comma, ) or ;. */
  private static int skipExpression(String source, int i) {
    int depth = 0;
    for (; i < source.length(); i++) {
      char c = source.charAt(i);
      if (c == '(') {
        depth++;
      } else if (c == ')') {
        if (depth == 0) {
          return i;
        }
        depth--;
      } else if (depth == 0 && (c == '+' || c == ',' || c == ';')) {
        return i;
      }
    }
    return i;
  }

  @Test
  void pathParsingHandlesConcatenation() {
    assertEquals(
        Collections.singletonList("/v3/users/{}/report_fraud"),
        pathsIn(".path(\"/v3/users/\" + encode(id) + \"/report_fraud\")", "\"/v3/"));
    assertEquals(
        Collections.singletonList("/v3/aml/monitoring/{}/news/{}"),
        pathsIn(
            ".path(\"/v3/aml/monitoring/\"\n + enc(a)\n + \"/news/\"\n + enc(b))\n .x()",
            "\"/v3/"));
    assertEquals(
        Collections.singletonList("/v3/basic_kyc"),
        pathsIn("request(\"/v3/basic_kyc\", params)", "\"/v3/"));
  }
}
