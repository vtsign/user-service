package tech.vtsign.userservice.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class TransactionMoneyDto {
    private UUID id;
    private String status;
    private String method;
    private long amount;
    private String description;
    @JsonProperty("created_date")
    private LocalDateTime createdDate;
}
