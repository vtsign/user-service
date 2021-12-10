package tech.vtsign.userservice.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionMoney {
    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(name = "transaction_uuid", unique = true, updatable = false, columnDefinition = "BINARY(16)")
    private UUID id;

    private String status;
    private String method;
    private long amount;
    private String description;
    @ManyToOne
    @JoinColumn(name = "user_uuid")
    private User user;
}
