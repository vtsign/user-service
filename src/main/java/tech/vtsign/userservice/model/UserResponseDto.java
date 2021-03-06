package tech.vtsign.userservice.model;

import com.fasterxml.jackson.annotation.JsonProperty;
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
    @JsonProperty("first_name")
    private String firstName;
    @JsonProperty("last_name")
    private String lastName;
    private String phone;
    private String organization;
    private String address;
    private long balance;
    private String avatar;
    private boolean enabled;
    private boolean blocked;
    private boolean deleted;
    private List<Role> roles;
    private List<Permission> permissions;
}
