package tech.vtsign.userservice.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class UserRequestDto {
    private String email;
    private String password;
    private String firstName;
    private String lastName;
    private String phone;
    private String organization;
    private String address;
}
