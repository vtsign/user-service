package tech.vtsign.userservice.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import tech.vtsign.userservice.domain.Permission;
import tech.vtsign.userservice.domain.Role;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserResponseDto {
    private UUID id;
    private String email;
    private String firstName;
    private String lastName;
    private String phone;
    private String organization;
    private String address;
    private boolean enabled;
    private List<Role> roles;
    private List<Permission> permissions;
}
