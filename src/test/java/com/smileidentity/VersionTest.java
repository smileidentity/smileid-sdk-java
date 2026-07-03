package com.smileidentity;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class VersionTest {

  @Test
  void versionMatchesExpected() {
    assertEquals("12.0.0", Version.VERSION);
  }
}
