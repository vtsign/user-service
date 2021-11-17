package tech.vtsign.userservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tech.vtsign.userservice.domain.User;
import tech.vtsign.userservice.service.UserService;

@RequiredArgsConstructor
@RestController
public class UserController {

    private final UserService userService;

    @Operation(summary = "Check user exists by email")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "true: exists, false: not exists",
                    content = @Content
            ),
    })
    @GetMapping("check_exists")
    public ResponseEntity<?> checkUserExistByEmail(@RequestParam ("email") String email) {
        // tai khoan tam xem nhu chua co tai khoan
        boolean exists = true;
        try {
            User user = userService.findByEmail(email);
            if (user.isTempAccount()) {
                exists = false;
            }
        } catch (Exception e) { // UsernameNotFoundException
           exists = false;
        }

        return ResponseEntity.ok(exists);
    }

}
