package tech.vtsign.userservice.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tech.vtsign.userservice.domain.User;
import tech.vtsign.userservice.exception.BadRequestException;
import tech.vtsign.userservice.exception.ExceptionResponse;
import tech.vtsign.userservice.exception.MissingFieldException;
import tech.vtsign.userservice.model.*;
import tech.vtsign.userservice.model.zalopay.ZaloPayResponse;
import tech.vtsign.userservice.security.UserDetailsImpl;
import tech.vtsign.userservice.service.UserService;

import javax.servlet.ServletContext;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@RestController
@Tag(name = "User controller")
@Slf4j
public class UserController {

    private final UserService userService;
    private final ServletContext context;

    @Operation(summary = "Check User Exists By Email")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "true: exists, false: not exists",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Boolean.class))
            ),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = {
                            @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))
                    }),
            @ApiResponse(responseCode = "400", description = "Missing fields or accessToken",
                    content = {
                            @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))
                    }),
    })
    @GetMapping("check-exists")
    public ResponseEntity<Boolean> checkUserExistByEmail(@RequestParam("email") String email) {
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

    @Operation(summary = "Get User Profile")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Get user profile",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserResponseDto.class))
            ),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = {
                            @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))
                    }),
            @ApiResponse(responseCode = "400", description = "Missing fields or accessToken",
                    content = {
                            @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))
                    }),
    })
    @GetMapping("profile")
    public ResponseEntity<UserResponseDto> getProfile(@Parameter(hidden = true) @AuthenticationPrincipal
                                                              UserDetailsImpl userDetails) {
        UserResponseDto user = userDetails.getUser();
        return ResponseEntity.ok(user);
    }

    @Operation(summary = "Update User Profile")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Update user profile",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserResponseDto.class))
            ),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = {
                            @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))
                    }),
            @ApiResponse(responseCode = "400", description = "Missing fields or accessToken",
                    content = {
                            @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))
                    }),

    })

    @PostMapping("profile")
    public ResponseEntity<UserResponseDto> updateProfile(@Parameter(hidden = true) @AuthenticationPrincipal
                                                                 UserDetailsImpl userDetails,
                                                         @RequestBody UserUpdateDto userUpdateDto) {
        UserResponseDto user = userDetails.getUser();
        userUpdateDto.setRole(null);
        User updatedUser = userService.updateUser(user.getId(), userUpdateDto);
        BeanUtils.copyProperties(updatedUser, user);
        return ResponseEntity.ok(user);
    }

    @Operation(summary = "User Update Password")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Change password successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserResponseDto.class))
            ),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = {
                            @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))
                    }),
            @ApiResponse(responseCode = "403", description = "Old password is incorrect",
                    content = {
                            @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))
                    }),
            @ApiResponse(responseCode = "419", description = "Missing require field see message for more details",
                    content = {
                            @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))
                    }),
            @ApiResponse(responseCode = "400", description = "Missing fields or accessToken",
                    content = {
                            @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))
                    }),
    })
    @PostMapping("change-password")
    public ResponseEntity<UserResponseDto> changePassword(@Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails,
                                                          @RequestBody @Validated UserChangePasswordDto userChangePasswordDto, BindingResult result) {
        if (result.hasErrors()) {
            String errorMessage = result.getAllErrors()
                    .stream().map(DefaultMessageSourceResolvable::getDefaultMessage)
                    .collect(Collectors.joining(";"));
            throw new MissingFieldException(errorMessage);
        }
        UserResponseDto user = userDetails.getUser();
        User updatedUser = userService.changePassword(user.getId(), userChangePasswordDto);
        BeanUtils.copyProperties(updatedUser, user);
        return ResponseEntity.ok(user);
    }

    @Operation(summary = "User Update Password")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Change password successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserResponseDto.class))
            ),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = {
                            @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))
                    }),
            @ApiResponse(responseCode = "400", description = "File is not image or empty",
                    content = {
                            @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))
                    }),
    })
    @PostMapping("update-avatar")
    public ResponseEntity<UserResponseDto> updateAvatar(@Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails,
                                                        @RequestPart("avatar") MultipartFile file) {
        UserResponseDto user = userDetails.getUser();
        if (file.isEmpty()) {
            throw new BadRequestException("Avatar is empty");
        }
        String mimeType = context.getMimeType(file.getOriginalFilename());
        if (!mimeType.startsWith("image/")) {
            throw new BadRequestException("File is not image");
        }
        User updatedUser = userService.updateAvatar(user.getId(), file);
        BeanUtils.copyProperties(updatedUser, user);
        return ResponseEntity.ok(user);
    }

    @Operation(summary = "User Deposit money [CC, ATM, zalopayapp]")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ZaloPayResponse.class))
            ),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = {
                            @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))
                    }),
            @ApiResponse(responseCode = "419", description = "Missing require field see message for more details",
                    content = {
                            @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))
                    }),
            @ApiResponse(responseCode = "400", description = "Missing fields or accessToken",
                    content = {
                            @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))
                    }),
    })
    @PostMapping("/deposit")
    public ResponseEntity<ZaloPayResponse> depositMoney(@Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails,
                                                        @RequestBody @Validated UserDepositDto userDepositDto, BindingResult result)
            throws JsonProcessingException {
        if (result.hasErrors()) {
            String errorMessage = result.getAllErrors()
                    .stream().map(DefaultMessageSourceResolvable::getDefaultMessage)
                    .collect(Collectors.joining(";"));
            throw new MissingFieldException(errorMessage);
        }
        ZaloPayResponse zaloPayResponse = userService.deposit(userDetails.getUser().getId(), userDepositDto);
        return ResponseEntity.ok(zaloPayResponse);
    }

    @Operation(summary = "List Transactions Of User")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = DTOList.class))
            ),
            @ApiResponse(responseCode = "419", description = "Missing require field see message for more details",
                    content = {
                            @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))
                    }),
            @ApiResponse(responseCode = "400", description = "Missing fields or accessToken",
                    content = {
                            @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))
                    }),
    })

    @GetMapping("/transactions")
    public ResponseEntity<DTOList<?>> findAllTransactions(@Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails,
                                                          @RequestParam(value = "page", required = false, defaultValue = "1") int page,
                                                          @RequestParam(value = "size", required = false, defaultValue = "4") int size,
                                                          @RequestParam(name = "sort_field", required = false, defaultValue = "createdDate") String sortField,
                                                          @RequestParam(name = "sort_type", required = false, defaultValue = "desc") String sortType) {
        User user = new User();
        user.setId(userDetails.getUser().getId());
        DTOList<?> transactions = userService.getTransactionManagementList(user, page, size, sortField, sortType);
        return ResponseEntity.ok(transactions);
    }

    @Operation(summary = "Get Maximum Receivers To Sign Contract")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Long.class))
            ),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = {
                            @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))
                    }),
            @ApiResponse(responseCode = "400", description = "Missing fields or accessToken",
                    content = {
                            @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))
                    }),
    })
    @GetMapping("/max-receivers")
    public ResponseEntity<Long> maxReceivers(@Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl userDetails) {
        Long maxReceivers = userService.maxReceivers(userDetails.getUser().getId());
        return ResponseEntity.ok(maxReceivers);
    }

}