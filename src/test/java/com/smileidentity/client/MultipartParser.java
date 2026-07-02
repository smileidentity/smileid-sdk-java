package com.smileidentity.client;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import okhttp3.mockwebserver.RecordedRequest;

/** Minimal multipart/form-data parser for asserting exact wire shape in tests. */
final class MultipartParser {

  static final class TestPart {
    final String name;
    final String filename;
    final String contentType;
    final String body;

    TestPart(String name, String filename, String contentType, String body) {
      this.name = name;
      this.filename = filename;
      this.contentType = contentType;
      this.body = body;
    }
  }

  private MultipartParser() {}

  static List<TestPart> parse(RecordedRequest request) {
    String contentType = request.getHeader("Content-Type");
    if (contentType == null || !contentType.startsWith("multipart/form-data")) {
      throw new AssertionError("Not a multipart request: " + contentType);
    }
    Matcher m = Pattern.compile("boundary=([^;]+)").matcher(contentType);
    if (!m.find()) {
      throw new AssertionError("No boundary in " + contentType);
    }
    String boundary = m.group(1).trim();
    String body = new String(request.getBody().readByteArray(), StandardCharsets.UTF_8);
    String delimiter = "--" + boundary;
    List<TestPart> parts = new ArrayList<>();
    for (String section : body.split(Pattern.quote(delimiter))) {
      String chunk = section;
      if (chunk.startsWith("\r\n")) {
        chunk = chunk.substring(2);
      }
      if (chunk.isEmpty() || chunk.startsWith("--")) {
        continue;
      }
      int split = chunk.indexOf("\r\n\r\n");
      if (split < 0) {
        continue;
      }
      String headerBlock = chunk.substring(0, split);
      String partBody = chunk.substring(split + 4);
      if (partBody.endsWith("\r\n")) {
        partBody = partBody.substring(0, partBody.length() - 2);
      }
      String name = null;
      String filename = null;
      String partContentType = null;
      for (String line : headerBlock.split("\r\n")) {
        String lower = line.toLowerCase();
        if (lower.startsWith("content-disposition:")) {
          Matcher nameMatcher = Pattern.compile("name=\"([^\"]*)\"").matcher(line);
          if (nameMatcher.find()) {
            name = nameMatcher.group(1);
          }
          Matcher fileMatcher = Pattern.compile("filename=\"([^\"]*)\"").matcher(line);
          if (fileMatcher.find()) {
            filename = fileMatcher.group(1);
          }
        } else if (lower.startsWith("content-type:")) {
          partContentType = line.substring(line.indexOf(':') + 1).trim();
        }
      }
      parts.add(new TestPart(name, filename, partContentType, partBody));
    }
    return parts;
  }

  static List<TestPart> byName(List<TestPart> parts, String name) {
    List<TestPart> found = new ArrayList<>();
    for (TestPart p : parts) {
      if (p.name.equals(name)) {
        found.add(p);
      }
    }
    return found;
  }

  static TestPart single(List<TestPart> parts, String name) {
    List<TestPart> found = byName(parts, name);
    if (found.size() != 1) {
      throw new AssertionError("Expected exactly one part '" + name + "', got " + found.size());
    }
    return found.get(0);
  }

  static boolean has(List<TestPart> parts, String name) {
    return !byName(parts, name).isEmpty();
  }
}
