package tech.vtsign.userservice.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserStatusDto {
    @JsonProperty("id")
    private UUID userId;
    private boolean status;
}
