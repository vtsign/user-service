package tech.vtsign.userservice.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class UserChangePasswordDto {
    @NotBlank(message = "Missing old password")
    @JsonProperty("old_password")
    private String oldPassword;
    @NotBlank(message = "Missing new password")
    @JsonProperty("new_password")
    private String newPassword;
}
