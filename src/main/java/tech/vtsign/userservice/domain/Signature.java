package tech.vtsign.userservice.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;
import tech.vtsign.userservice.model.SignatureType;

import javax.persistence.*;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Signature {
    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(name = "signature_uuid", unique = true, updatable = false, columnDefinition = "BINARY(16)")
    private UUID id;
    private String url;
    private String type = SignatureType.GENERATED;
    @JsonIgnore
    @ManyToOne()
    @JoinColumn(name = "user_uuid")
    private User user;
}
