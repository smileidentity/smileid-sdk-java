package com.smileidentity.client;

/** Injectable sleep, so retry backoff is testable without real waits. */
interface Sleeper {
  void sleep(long millis) throws InterruptedException;

  Sleeper DEFAULT = Thread::sleep;
}
