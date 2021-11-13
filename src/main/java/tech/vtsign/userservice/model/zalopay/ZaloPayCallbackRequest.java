package tech.vtsign.userservice.model.zalopay;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ZaloPayCallbackRequest {
    private String data;
    private String mac;
    private String type;
}
