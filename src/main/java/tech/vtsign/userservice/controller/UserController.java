package tech.vtsign.userservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import tech.vtsign.userservice.domain.User;
import tech.vtsign.userservice.exception.ExceptionResponse;
import tech.vtsign.userservice.model.UserResponseDto;
import tech.vtsign.userservice.model.UserUpdateDto;
import tech.vtsign.userservice.security.UserDetailsImpl;
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
    public ResponseEntity<?> checkUserExistByEmail(@RequestParam("email") String email) {
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

    @Operation(summary = "Get user profile")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Get user profile",
                    content = @Content
            ),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = {
                            @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))
                    })
    })
    @GetMapping("profile")
    public ResponseEntity<?> getProfile(@Parameter(hidden = true) @AuthenticationPrincipal
                                                UserDetailsImpl userDetails) {
        UserResponseDto user = userDetails.getUser();
        return ResponseEntity.ok(user);
    }

    @Operation(summary = "Update user profile")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Update user profile",
                    content = @Content
            ),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = {
                            @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))
                    })
    })

    @PostMapping("profile")
    public ResponseEntity<?> updateProfile(@Parameter(hidden = true) @AuthenticationPrincipal
                                                   UserDetailsImpl userDetails,
                                           @RequestBody UserUpdateDto userUpdateDto) {
        UserResponseDto user = userDetails.getUser();
        User updatedUser = userService.updateUser(user.getId(), userUpdateDto);
        BeanUtils.copyProperties(updatedUser, user);
        return ResponseEntity.ok(user);
    }


}
