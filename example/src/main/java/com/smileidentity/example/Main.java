package com.smileidentity.example;

public final class Main {
  private Main() {}

  public static void main(String[] args) {
    int exitCode = new ExampleApp(System.getenv(), System.out, System.err, null).run(args);
    System.exit(exitCode);
  }
}
