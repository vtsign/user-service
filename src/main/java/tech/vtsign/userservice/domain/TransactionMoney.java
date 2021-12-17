package tech.vtsign.userservice.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionMoney extends Auditable<String> implements Serializable {
    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(name = "transaction_uuid", unique = true, updatable = false, columnDefinition = "BINARY(16)")
    private UUID id;

    private String status;
    private String method;
    private long amount;
    private String description;
    @JsonProperty("created_date")
    private LocalDateTime createdDate;
    @JsonIgnore
    @ToString.Exclude
    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    @JoinColumn(name = "user_uuid")
    private User user;
}
