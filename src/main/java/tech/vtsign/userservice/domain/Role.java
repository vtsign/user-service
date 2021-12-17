package tech.vtsign.userservice.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.List;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Role {
    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(name = "role_uuid", unique = true, updatable = false, columnDefinition = "BINARY(16)")
    private UUID id;

    private String name;
    private String description;

    public Role(String name) {
        this.name = name;
        this.description = name;
    }

    @JsonIgnore
    @ManyToMany(mappedBy = "roles")
    @ToString.Exclude
    private List<User> users;
}