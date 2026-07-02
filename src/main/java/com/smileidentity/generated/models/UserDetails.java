package com.smileidentity.generated.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * End-user details, required on all entry endpoints (spec §5.1). At least one of email or phone
 * number must be present; the SDK validates this before sending. Serialized as a JSON multipart
 * part named {@code user_details}.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"given_names", "last_name", "email", "phone_number"})
public final class UserDetails {

  @JsonProperty("given_names")
  private final String givenNames;

  @JsonProperty("last_name")
  private final String lastName;

  @JsonProperty("email")
  private final String email;

  @JsonProperty("phone_number")
  private final String phoneNumber;

  private UserDetails(Builder b) {
    this.givenNames = b.givenNames;
    this.lastName = b.lastName;
    this.email = b.email;
    this.phoneNumber = b.phoneNumber;
  }

  public static Builder builder() {
    return new Builder();
  }

  public String getGivenNames() {
    return givenNames;
  }

  public String getLastName() {
    return lastName;
  }

  public String getEmail() {
    return email;
  }

  public String getPhoneNumber() {
    return phoneNumber;
  }

  public static final class Builder {
    private String givenNames;
    private String lastName;
    private String email;
    private String phoneNumber;

    public Builder givenNames(String givenNames) {
      this.givenNames = givenNames;
      return this;
    }

    public Builder lastName(String lastName) {
      this.lastName = lastName;
      return this;
    }

    public Builder email(String email) {
      this.email = email;
      return this;
    }

    public Builder phoneNumber(String phoneNumber) {
      this.phoneNumber = phoneNumber;
      return this;
    }

    public UserDetails build() {
      return new UserDetails(this);
    }
  }
}
