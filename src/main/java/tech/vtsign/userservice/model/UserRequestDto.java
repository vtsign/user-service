package tech.vtsign.userservice.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class UserRequestDto {
    @Email(message = "Invalid email address")
    @NotBlank(message = "Missing email")
    private String email;
    @NotBlank(message = "Missing password")
    private String password;
    @JsonProperty("first_name")
    @NotBlank(message = "Missing first name")
    private String firstName;
    @JsonProperty("last_name")
    @NotBlank(message = "Missing last name")
    private String lastName;
    private String phone;
    private String organization;
    private String address;
}
