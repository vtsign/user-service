package tech.vtsign.userservice.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

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
    @Pattern(regexp = "(^(\\+\\d{1,2}\\s?)?1?\\-?\\.?\\s?\\(?\\d{3}\\)?[\\s.-]?\\d{3}[\\s.-]?\\d{4}$)",
            message = "Invalid phone number")
    @NotBlank(message = "Missing phone")
    private String phone;
    private String organization;
    private String address;
}
