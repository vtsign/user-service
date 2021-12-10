package tech.vtsign.userservice.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import javax.validation.constraints.Email;
import java.io.Serializable;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class User extends Auditable<String> implements Serializable {
    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(name = "user_uuid", unique = true, updatable = false, columnDefinition = "BINARY(16)")
    private UUID id;
    @Column(unique = true)
    @Email
    private String email;
    private String password;
    @JsonProperty("first_name")
    private String firstName;
    @JsonProperty("last_name")
    private String lastName;
    private String phone;
    private String organization;
    private String address;
    private long balance;
    private boolean enabled = false;
    private boolean blocked = false;
    @JsonProperty("temp_account")
    private boolean tempAccount = false;

    @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_uuid"),
            inverseJoinColumns = @JoinColumn(name = "role_uuid"))
    private List<Role> roles;

    @ManyToMany(cascade = CascadeType.ALL)
    @JoinTable(
            name = "user_permissions",
            joinColumns = @JoinColumn(name = "user_uuid"),
            inverseJoinColumns = @JoinColumn(name = "permission_uuid"))
    private List<Permission> permissions;

    public String getFullName() {
        return String.format("%s %s", this.firstName, this.lastName);
    }

}