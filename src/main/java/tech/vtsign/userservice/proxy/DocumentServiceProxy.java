package tech.vtsign.userservice.proxy;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import tech.vtsign.userservice.domain.User;
import tech.vtsign.userservice.model.UserUpdateDto;

@FeignClient(name = "document-service")
public interface DocumentServiceProxy {
        @PutMapping("/document/apt/update-user")
        User updateUser(@RequestBody User user);
        @PostMapping("/document/apt/save-user")
        User saveUser(@RequestBody User user);
}
