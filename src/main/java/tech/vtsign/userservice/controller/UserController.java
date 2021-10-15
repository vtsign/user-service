package tech.vtsign.userservice.controller;


import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.beans.BeanUtils;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import tech.vtsign.userservice.domain.User;
import tech.vtsign.userservice.exception.ExceptionResponse;
import tech.vtsign.userservice.exception.MissingFieldException;
import tech.vtsign.userservice.model.UserLoginDto;
import tech.vtsign.userservice.model.UserRequestDto;
import tech.vtsign.userservice.model.UserResponseDto;
import tech.vtsign.userservice.service.UserService;

import java.security.NoSuchAlgorithmException;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@RestController
@Tag(name="User controller")
public class UserController {

    private final UserService userService;

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
    public ResponseEntity<UserResponseDto> retrieveUserByEmail(@RequestParam String email, @RequestParam(required = false) String name) throws NoSuchAlgorithmException {
        User user = userService.getOrCreateUser(email, name);
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
        BeanUtils.copyProperties(userRequestDto, user);
        userService.save(user);
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
}
