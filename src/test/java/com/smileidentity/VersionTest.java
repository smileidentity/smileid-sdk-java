package com.smileidentity;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class VersionTest {

  @Test
  void versionMatchesExpected() {
    assertEquals("0.1.0", Version.VERSION);
  }
}
