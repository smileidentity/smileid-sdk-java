package com.smileidentity.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.smileidentity.client.SmileID;
import com.smileidentity.generated.models.Consent;
import com.smileidentity.generated.models.EnhancedKycParams;
import com.smileidentity.generated.models.SupportedDocumentsParams;
import com.smileidentity.generated.models.UserDetails;
import java.io.PrintStream;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import okhttp3.OkHttpClient;

public final class ExampleApp {
  private static final ObjectMapper JSON =
      new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

  private final Map<String, String> env;
  private final PrintStream stdout;
  private final PrintStream stderr;
  private final OkHttpClient httpClient;

  public ExampleApp(
      Map<String, String> env, PrintStream stdout, PrintStream stderr, OkHttpClient httpClient) {
    this.env = env;
    this.stdout = stdout;
    this.stderr = stderr;
    this.httpClient = httpClient;
  }

  public int run(String[] argv) {
    try {
      Parsed parsed = parse(argv);
      if (parsed.command == null) {
        throw new UsageException(
            "missing command; run one of: services, enhanced-kyc, status, replay");
      }
      if (parsed.command.equals("help")
          || parsed.command.equals("-h")
          || parsed.command.equals("--help")) {
        stdout.print(usage());
        return 0;
      }
      validate(parsed.config);
      SmileID smile = client(parsed.config);
      switch (parsed.command) {
        case "services":
          services(smile, parsed.args);
          return 0;
        case "enhanced-kyc":
          enhancedKyc(smile, parsed.args, parsed.config);
          return 0;
        case "status":
          status(smile, parsed.args);
          return 0;
        case "replay":
          replay(smile, parsed.args);
          return 0;
        default:
          throw new UsageException("unknown command " + parsed.command);
      }
    } catch (UsageException e) {
      stderr.println(e.getMessage());
      return 2;
    } catch (Exception e) {
      stderr.println(e.getMessage());
      return 1;
    }
  }

  private SmileID client(Map<String, String> config) {
    long timeoutMs;
    try {
      timeoutMs = Long.parseLong(config.get("timeoutMs"));
    } catch (NumberFormatException e) {
      throw new UsageException("--timeout-ms must be a positive integer number of milliseconds");
    }
    if (timeoutMs <= 0) {
      throw new UsageException("--timeout-ms must be a positive integer number of milliseconds");
    }
    SmileID.Builder builder =
        SmileID.builder()
            .partnerId(config.get("partnerId"))
            .apiKey(config.get("apiKey"))
            .timeout(Duration.ofMillis(timeoutMs));
    if (present(config.get("baseUrl"))) builder.baseUrl(config.get("baseUrl"));
    if (present(config.get("callbackUrl"))) builder.defaultCallbackUrl(config.get("callbackUrl"));
    if (httpClient != null) builder.httpClient(httpClient);
    return builder.build();
  }

  private void services(SmileID smile, String[] args) throws Exception {
    String country = flag(args, "--country", "NG");
    var services = smile.services();
    Map<String, Object> out = new LinkedHashMap<>();
    out.put("country", country);
    out.put("bank_codes", services.bankCodes(country).getBankCodes());
    out.put("id_types", services.supportedIdTypes(country).getIdTypes());
    out.put(
        "documents",
        services
            .supportedDocuments(SupportedDocumentsParams.builder().countryCode(country).build())
            .getValidDocuments());
    write(out);
  }

  private void enhancedKyc(SmileID smile, String[] args, Map<String, String> config)
      throws Exception {
    String callbackUrl = flag(args, "--callback-url", config.get("callbackUrl"));
    EnhancedKycParams.Builder builder =
        EnhancedKycParams.builder()
            .country(flag(args, "--country", "NG"))
            .idType(required(args, "--id-type"))
            .idNumber(required(args, "--id-number"))
            .userDetails(
                UserDetails.builder()
                    .givenNames(required(args, "--given-names"))
                    .lastName(required(args, "--last-name"))
                    .email(flag(args, "--email", null))
                    .phoneNumber(flag(args, "--phone-number", null))
                    .build())
            .consent(
                Consent.granted(
                    Instant.now(),
                    "EN",
                    flag(args, "--privacy-url", "https://example.com/privacy")));
    if (present(callbackUrl)) builder.callbackUrl(callbackUrl);
    var accepted = smile.enhancedKyc().verify(builder.build());
    Map<String, Object> out = new LinkedHashMap<>();
    out.put("status", accepted.getStatus());
    out.put("message", accepted.getMessage());
    out.put("job_id", accepted.getJobId());
    out.put("user_id", accepted.getUserId());
    out.put("accepted", accepted.isAccepted());
    write(out);
  }

  private void status(SmileID smile, String[] args) throws Exception {
    var status = smile.verifications().retrieve(required(args, "--job-id"));
    Map<String, Object> out = new LinkedHashMap<>();
    out.put("status", status.getStatus());
    out.put("message", status.getMessage());
    out.put("job_id", status.getJobId());
    out.put("user_id", status.getUserId());
    write(out);
  }

  private void replay(SmileID smile, String[] args) throws Exception {
    var replay =
        smile
            .verifications()
            .replay(
                required(args, "--job-id"),
                com.smileidentity.generated.models.ReplayParams.builder()
                    .callbackUrl(flag(args, "--callback-url", null))
                    .build());
    Map<String, Object> out = new LinkedHashMap<>();
    out.put("status", replay.getStatus());
    out.put("message", replay.getMessage());
    out.put("job_id", replay.getJobId());
    out.put("user_id", replay.getUserId());
    write(out);
  }

  private Parsed parse(String[] argv) {
    Map<String, String> config = new LinkedHashMap<>();
    config.put("partnerId", env.getOrDefault("SMILE_PARTNER_ID", ""));
    config.put("apiKey", env.getOrDefault("SMILE_API_KEY", ""));
    config.put("baseUrl", env.get("SMILE_BASE_URL"));
    config.put("callbackUrl", env.get("SMILE_CALLBACK_URL"));
    config.put("timeoutMs", env.getOrDefault("SMILE_TIMEOUT_MS", "30000"));
    int i = 0;
    while (i < argv.length && argv[i].startsWith("--")) {
      String flag = argv[i];
      if (Objects.equals(flag, "--help") || Objects.equals(flag, "-h")) {
        break;
      }
      if (i + 1 >= argv.length) {
        throw new UsageException(flag + " requires a value");
      }
      String value = argv[++i];
      switch (flag) {
        case "--partner-id":
          config.put("partnerId", value);
          break;
        case "--api-key":
          config.put("apiKey", value);
          break;
        case "--base-url":
          config.put("baseUrl", value);
          break;
        case "--callback-url":
          config.put("callbackUrl", value);
          break;
        case "--timeout-ms":
          config.put("timeoutMs", value);
          break;
        default:
          throw new UsageException("unknown global flag " + flag);
      }
      i++;
    }
    String command = i < argv.length ? argv[i] : null;
    String[] args = new String[Math.max(0, argv.length - i - 1)];
    if (args.length > 0) System.arraycopy(argv, i + 1, args, 0, args.length);
    return new Parsed(config, command, args);
  }

  private void validate(Map<String, String> config) {
    if (!present(config.get("partnerId")) || !present(config.get("apiKey"))) {
      throw new UsageException(
          "missing SMILE_PARTNER_ID or --partner-id and SMILE_API_KEY or --api-key");
    }
  }

  private String required(String[] args, String name) {
    String value = flag(args, name, null);
    if (!present(value)) throw new UsageException(name + " is required");
    return value;
  }

  private String flag(String[] args, String name, String fallback) {
    for (int i = 0; i < args.length - 1; i++) {
      if (args[i].equals(name)) return args[i + 1];
    }
    return fallback;
  }

  private boolean present(String value) {
    return value != null && !value.isEmpty();
  }

  private void write(Object value) throws Exception {
    stdout.println(JSON.writeValueAsString(value));
  }

  private String usage() {
    return "Usage:\n"
        + "  smileid-example-java [global flags] services --country NG\n"
        + "  smileid-example-java [global flags] enhanced-kyc --country NG --id-type NIN --id-number 12345678901 --given-names 'Amina Fatou' --last-name Clearwater --email amina.clearwater@example.com --privacy-url https://example.com/privacy\n"
        + "  smileid-example-java [global flags] status --job-id job_...\n"
        + "  smileid-example-java [global flags] replay --job-id job_... --callback-url https://example.com/webhook\n\n"
        + "Global flags can also be set with SMILE_PARTNER_ID, SMILE_API_KEY, SMILE_BASE_URL, SMILE_CALLBACK_URL and SMILE_TIMEOUT_MS.\n"
        + "Partner ids are displayed zero-padded (for example 002) but must be passed without the leading zeros (2).\n"
        + "SMILE_BASE_URL (or --base-url) points the SDK at a host such as https://your-environment.example.com; without it the sandbox is used.\n"
        + "Non-production environments match test identities on given names, last name and email. An unrecognised identity resolves to block.\n";
  }

  private static final class Parsed {
    final Map<String, String> config;
    final String command;
    final String[] args;

    Parsed(Map<String, String> config, String command, String[] args) {
      this.config = config;
      this.command = command;
      this.args = args;
    }
  }

  private static final class UsageException extends RuntimeException {
    UsageException(String message) {
      super(message);
    }
  }
}
