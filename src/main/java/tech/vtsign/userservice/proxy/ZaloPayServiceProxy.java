package tech.vtsign.userservice.proxy;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import tech.vtsign.userservice.model.zalopay.OAOrder;
import tech.vtsign.userservice.model.zalopay.ZaloPayResponse;

@FeignClient(url = "https://sb-openapi.zalopay.vn/v2", name = "zalopay")
public interface ZaloPayServiceProxy {
    @PostMapping("/create")
    ZaloPayResponse createOrder(@RequestBody OAOrder oaOrder);
}
