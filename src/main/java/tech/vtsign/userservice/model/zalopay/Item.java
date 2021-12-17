package tech.vtsign.userservice.model.zalopay;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
public class Item {
    private UUID id;
    @JsonProperty("user_id")
    private UUID userId;
    private long amount;
    private String method;
    private String status;

    public Item(UUID id, UUID userId, long amount, String method) {
        this.id = id;
        this.userId = userId;
        this.amount = amount;
        this.method = method;
    }
}
