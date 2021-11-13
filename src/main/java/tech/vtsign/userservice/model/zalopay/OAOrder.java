package tech.vtsign.userservice.model.zalopay;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OAOrder {
    @JsonProperty("app_id")
    private int appId;
    @JsonProperty("app_user")
    private String appUser;
    @JsonProperty("app_trans_id")
    private String appTransId;
    @JsonProperty("app_time")
    private long appTime;
    private int amount;
    private String item;
    private String description;
    @JsonProperty("embed_data")
    private String embedData;
    @JsonProperty("bank_code")
    private String bankCode;
    private String mac;
    @JsonProperty("callback_url")
    private String callbackUrl;
    @JsonProperty("redirect_url")
    private String redirectUrl;
    @JsonProperty("device_info")
    private String deviceInfo;
    private String title;
    private String currency;
    private String phone;
    private String email;
    private String address;

}
