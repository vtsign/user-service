package tech.vtsign.userservice.controller;


import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import tech.vtsign.userservice.constant.RoleName;
import tech.vtsign.userservice.domain.ResetLink;
import tech.vtsign.userservice.domain.Role;
import tech.vtsign.userservice.domain.User;
import tech.vtsign.userservice.exception.ExceptionResponse;
import tech.vtsign.userservice.exception.MissingFieldException;
import tech.vtsign.userservice.model.ResetPasswordRequestDto;
import tech.vtsign.userservice.model.UserLoginDto;
import tech.vtsign.userservice.model.UserRequestDto;
import tech.vtsign.userservice.model.UserResponseDto;
import tech.vtsign.userservice.model.zalopay.Item;
import tech.vtsign.userservice.model.zalopay.ZaloPayCallbackRequest;
import tech.vtsign.userservice.service.RoleService;
import tech.vtsign.userservice.service.UserService;

import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@RestController
@Tag(name = "Accept controller")
@Slf4j
@RequestMapping("/apt")
public class AcceptController {

    private final UserService userService;
    private final RoleService roleService;

    @Hidden
    @Operation(summary = "Get user by email [service call only]")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found the user",
                    content = {
                            @Content(mediaType = "application/json", schema = @Schema(implementation = UserResponseDto.class))
                    }),
            @ApiResponse(responseCode = "422", description = "Invalid email format",
                    content = {
                            @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))
                    }),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = {
                            @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))
                    })
    })
    @GetMapping("/")
    public ResponseEntity<UserResponseDto> retrieveUser(@RequestParam("email") String email) {
        User user = userService.findByEmail(email);
        UserResponseDto userRes = new UserResponseDto();
        BeanUtils.copyProperties(user, userRes);
        return ResponseEntity.ok().body(userRes);
    }

    @Hidden
    @GetMapping("/email")
    public ResponseEntity<UserResponseDto> retrieveUserByEmail(@RequestParam String email,
                                                               @RequestParam(required = false) String phone,
                                                               @RequestParam(required = false) String name)
            throws NoSuchAlgorithmException {
        User user = userService.getOrCreateUser(email, phone, name);
        UserResponseDto userRes = new UserResponseDto();
        BeanUtils.copyProperties(user, userRes);
        return ResponseEntity.ok().body(userRes);
    }

    @Hidden
    @GetMapping("/uuid")
    public ResponseEntity<UserResponseDto> retrieveUser(@RequestParam("user_uuid") UUID userUUID) {
        System.out.println("User id: " + userUUID);
        User user = userService.findUserById(userUUID);
        UserResponseDto userRes = new UserResponseDto();
        BeanUtils.copyProperties(user, userRes);
        return ResponseEntity.ok().body(userRes);
    }

    @Hidden
    @Operation(summary = "Register account [service call only]")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Success, user registered",
                    content = {
                            @Content(mediaType = "application/json", schema = @Schema(implementation = UserResponseDto.class))
                    }),
            @ApiResponse(responseCode = "419", description = "Missing require field see message for more details",
                    content = {
                            @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))
                    }),
            @ApiResponse(responseCode = "409", description = "Email is already in use",
                    content = {
                            @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))
                    })
    })
    @PostMapping("/register")
    public ResponseEntity<UserResponseDto> register(@Validated @RequestBody UserRequestDto userRequestDto, BindingResult result) {
        if (result.hasErrors()) {
            String errorMessage = result.getAllErrors()
                    .stream().map(DefaultMessageSourceResolvable::getDefaultMessage)
                    .collect(Collectors.joining(";"));

            throw new MissingFieldException(errorMessage);
        }
        User user = new User();
        Role roleUser = roleService.findByName(RoleName.ROLE_USER);
        user.setRoles(Collections.singletonList(roleUser));
        BeanUtils.copyProperties(userRequestDto, user);
        userService.save(user);
        UserResponseDto responseDto = new UserResponseDto();
        BeanUtils.copyProperties(user, responseDto);

        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);

    }

    @Hidden
    @Operation(summary = "Register account [service call only]")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Success, user registered",
                    content = {
                            @Content(mediaType = "application/json", schema = @Schema(implementation = UserResponseDto.class))
                    }),
            @ApiResponse(responseCode = "419", description = "Missing require field see message for more details",
                    content = {
                            @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))
                    }),
            @ApiResponse(responseCode = "409", description = "Email is already in use",
                    content = {
                            @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))
                    })
    })
    @PostMapping("/register2")
    public ResponseEntity<UserResponseDto> register2(@Validated @RequestBody UserRequestDto userRequestDto, BindingResult result) {
        if (result.hasErrors()) {
            String errorMessage = result.getAllErrors()
                    .stream().map(DefaultMessageSourceResolvable::getDefaultMessage)
                    .collect(Collectors.joining(";"));

            throw new MissingFieldException(errorMessage);
        }
        User user = new User();
        BeanUtils.copyProperties(userRequestDto, user);
        userService.save2(user);
        UserResponseDto responseDto = new UserResponseDto();
        BeanUtils.copyProperties(user, responseDto);

        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);

    }

    @Hidden
    @Operation(summary = "Login account [service call only]")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login successfully",
                    content = {
                            @Content(mediaType = "application/json", schema = @Schema(implementation = UserResponseDto.class))
                    }),
            @ApiResponse(responseCode = "423", description = "User inactive",
                    content = {
                            @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))
                    }),
            @ApiResponse(responseCode = "403", description = "Invalid email password",
                    content = {
                            @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))
                    }),
            @ApiResponse(responseCode = "419", description = "Invalid email format",
                    content = {
                            @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))
                    }),
            @ApiResponse(responseCode = "419", description = "Email or password missing",
                    content = {
                            @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))
                    }),
    })
    @PostMapping("/login")
    public ResponseEntity<UserResponseDto> login(@Validated @RequestBody UserLoginDto userLoginDto, BindingResult result) {
        if (result.hasErrors()) {
            String errorMessage = result.getAllErrors()
                    .stream().map(DefaultMessageSourceResolvable::getDefaultMessage)
                    .collect(Collectors.joining(";"));

            throw new MissingFieldException(errorMessage);
        }
        Optional<User> opt = userService.login(userLoginDto.getEmail(), userLoginDto.getPassword());
        UserResponseDto userResponseDto = new UserResponseDto();
        BeanUtils.copyProperties(opt.get(), userResponseDto);
        return ResponseEntity.status(HttpStatus.OK).body(userResponseDto);
    }


    @SneakyThrows
    @Operation(summary = "Account activation")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success, Account has been activated",
                    content = {
                            @Content(mediaType = "application/json", schema = @Schema(implementation = Boolean.class))
                    }),
            @ApiResponse(responseCode = "400", description = "Link active not exist",
                    content = {
                            @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))
                    }),
            @ApiResponse(responseCode = "410", description = "Link expired",
                    content = {
                            @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))
                    })
    })

    @GetMapping("/activation/{id}")
    public ResponseEntity<Boolean> activation(@PathVariable UUID id) throws NoSuchAlgorithmException {
        boolean active = userService.activation(id);
        return ResponseEntity.ok(active);
    }

    @Operation(summary = "Create a reset password request")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success, Link has been sent to your email",
                    content = {
                            @Content(mediaType = "application/json", schema = @Schema(implementation = Boolean.class))
                    }),
            @ApiResponse(responseCode = "404", description = "Not found user with this email",
                    content = {
                            @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))
                    }),
    })

    @GetMapping("/reset-password")
    public boolean reset(@RequestParam(name = "email") String email) {
        userService.resetPassword(email);
        return true;
    }

    @Operation(summary = "Check reset password correct")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success, Link active valid",
                    content = {
                            @Content(mediaType = "application/json", schema = @Schema(implementation = Boolean.class))
                    }),
            @ApiResponse(responseCode = "404", description = "Link active not exist",
                    content = {
                            @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))
                    }),
            @ApiResponse(responseCode = "400", description = "Link invalid",
                    content = {
                            @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))
                    })
    })

    @GetMapping("/check-reset-password")
    public ResponseEntity<Boolean> checkResetLink(@RequestParam(name = "code") UUID code) {
        ResetLink resetLink = userService.checkRestLink(code);
        return ResponseEntity.ok(resetLink != null);
    }

    @Operation(summary = "Reset user password")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success, user password has been changed",
                    content = {
                            @Content(mediaType = "application/json", schema = @Schema(implementation = Boolean.class))
                    }),
            @ApiResponse(responseCode = "404", description = "Link active not exist",
                    content = {
                            @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))
                    }),
            @ApiResponse(responseCode = "400", description = "Link invalid or expired",
                    content = {
                            @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))
                    })
    })

    @PostMapping("/reset-password")
    public ResponseEntity<Boolean> resetPassword(@RequestBody ResetPasswordRequestDto request) {
        return ResponseEntity.ok(userService.resetPassword(request.getCode(), request.getPassword()));
    }

    @Operation(summary = "Zalopay callback")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "https://docs.zalopay.vn/v2/general/overview.html",
                    content = {
                            @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))
                    }),
    })
    @PostMapping("/deposit/callback")
    public ResponseEntity<String> orderCallback(
            @RequestBody ZaloPayCallbackRequest zaloPayCallbackRequest) throws JsonProcessingException {
        log.info("orderCallback: {}", zaloPayCallbackRequest);
        String json = userService.updateUserBalance(zaloPayCallbackRequest);
        return ResponseEntity.ok(json);
    }


    @Hidden
    @PostMapping("/payment")
    public ResponseEntity<Boolean> paymentForSendDocument(@RequestBody Item item) {
        Boolean result = userService.updateUserBalance(item.getUserId(), item.getAmount(), item.getStatus());
        return ResponseEntity.ok(result);
    }

}
