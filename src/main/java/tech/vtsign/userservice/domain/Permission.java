package tech.vtsign.userservice.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.List;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Permission {
    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(name = "permission_uuid", unique = true, updatable = false, columnDefinition = "BINARY(16)")
    private UUID id;
    private String name;
    private String description;
    @JsonIgnore
    @ManyToMany(mappedBy = "permissions")
    private List<User> users;
}
