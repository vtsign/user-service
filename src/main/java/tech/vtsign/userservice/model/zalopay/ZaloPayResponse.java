package tech.vtsign.userservice.model.zalopay;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ZaloPayResponse {
    @JsonProperty("return_code")
    public int code;
    @JsonProperty("return_message")
    public String message;
    @JsonProperty("sub_return_code")
    public int subCode;
    @JsonProperty("sub_return_message")
    public String subMessage;
    @JsonProperty("zp_trans_token")
    public String transToken;
    @JsonProperty("order_url")
    public String orderUrl;
    @JsonProperty("order_token")
    public String orderToken;
}

