package tech.vtsign.userservice.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserResponseDto {
    private String email;
    private String firstName;
    private String lastName;
    private String phone;
    private String organization;
    private String address;
    private boolean enabled;
}
