package tech.vtsign.userservice.model.zalopay;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ZaloPayCallBackResponse {
    @JsonProperty("return_code")
    private int code;
    @JsonProperty("return_message")
    private String message;
}
