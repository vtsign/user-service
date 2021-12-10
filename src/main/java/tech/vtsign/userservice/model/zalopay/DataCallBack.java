package tech.vtsign.userservice.model.zalopay;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class DataCallBack {
    @JsonProperty("app_id")
    private int appId;
    @JsonProperty("app_trans_id")
    private String appTransId;
    @JsonProperty("app_user")
    private String appUser;
    @JsonProperty("app_time")
    private long appTime;
    private long amount;
    @JsonProperty("embed_data")
    private String embedData;
    private String item;
    @JsonProperty("zp_trans_id")
    private long zpTransId;
    @JsonProperty("server_time")
    private long serverTime;
    private int channel;
    @JsonProperty("merchant_user_id")
    private String merchantUserId;
    @JsonProperty("user_fee_amount")
    private long userFeeAmount;
    @JsonProperty("discount_amount")
    private long discountAmount;
}
