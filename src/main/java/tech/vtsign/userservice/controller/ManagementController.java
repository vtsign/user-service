package tech.vtsign.userservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import tech.vtsign.userservice.constant.TransactionConstant;
import tech.vtsign.userservice.domain.Role;
import tech.vtsign.userservice.domain.User;
import tech.vtsign.userservice.exception.ExceptionResponse;
import tech.vtsign.userservice.exception.MissingFieldException;
import tech.vtsign.userservice.model.*;
import tech.vtsign.userservice.service.RoleService;
import tech.vtsign.userservice.service.UserService;
import tech.vtsign.userservice.utils.DateUtil;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/management")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class ManagementController {

    private final UserService userService;
    private final RoleService roleService;


    @Operation(summary = "" +
            "Get User By Id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
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
            @ApiResponse(responseCode = "403", description = "forbidden",
                    content = {
                            @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))
                    }),
    })

    @GetMapping("/customer/{id}")
    public UserResponseDto getUserById(@PathVariable UUID id) {
        User user = userService.findUserById(id);
        UserResponseDto userResponseDto = new UserResponseDto();
        BeanUtils.copyProperties(user, userResponseDto);
        return userResponseDto;
    }

    @Operation(summary = "" +
            "Get All Users Active")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = DTOList.class))
            ),

            @ApiResponse(responseCode = "400", description = "Missing fields or accessToken",
                    content = {
                            @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))
                    }),
            @ApiResponse(responseCode = "403", description = "forbidden",
                    content = {
                            @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))
                    }),
    })

    @GetMapping("/list")
    public DTOList<?> getManagement(
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "pageSize", defaultValue = "5") int pageSize,
            @RequestParam(name = "sortField", required = false, defaultValue = "firstName") String sortField,
            @RequestParam(name = "sortType", required = false, defaultValue = "asc") String sortType,
            @RequestParam(name = "keyword", required = false, defaultValue = "") String keyword
    ) {
        return userService.getUserManagementList(page, pageSize, sortField, sortType, keyword);
    }

    @Operation(summary = "" +
            "Get All Users Block")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = DTOList.class))
            ),

            @ApiResponse(responseCode = "400", description = "Missing fields or accessToken",
                    content = {
                            @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))
                    }),
            @ApiResponse(responseCode = "403", description = "forbidden",
                    content = {
                            @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))
                    }),
    })

    @GetMapping("/list-block")
    public DTOList getBlockedUsers(
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "pageSize", defaultValue = "5") int pageSize,
            @RequestParam(name = "sortField", required = false, defaultValue = "firstName") String sortField,
            @RequestParam(name = "sortType", required = false, defaultValue = "asc") String sortType,
            @RequestParam(name = "keyword", required = false, defaultValue = "") String keyword
    ) {
        return userService.getBlockedUsers(page, pageSize, sortField, sortType, keyword);
    }

    @Operation(summary = "" +
            "Get All Users Deleted")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = DTOList.class))
            ),

            @ApiResponse(responseCode = "400", description = "Missing fields or accessToken",
                    content = {
                            @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))
                    }),
            @ApiResponse(responseCode = "403", description = "forbidden",
                    content = {
                            @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))
                    }),
    })

    @GetMapping("/list-deleted")
    public DTOList<?> getDeletedUsers(
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "pageSize", defaultValue = "5") int pageSize,
            @RequestParam(name = "sortField", required = false, defaultValue = "firstName") String sortField,
            @RequestParam(name = "sortType", required = false, defaultValue = "asc") String sortType,
            @RequestParam(name = "keyword", required = false, defaultValue = "") String keyword
    ) {
        return userService.getDeletedUsers(page, pageSize, sortField, sortType, keyword);
    }

    @Operation(summary = "" +
            "Get All Users Active")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = List.class))
            ),
            @ApiResponse(responseCode = "403", description = "forbidden",
                    content = {
                            @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))
                    }),
    })

    @GetMapping("/roles")
    public ResponseEntity<?> retrieveAllRoles() {
        List<Role> roles = roleService.findAll();
        return ResponseEntity.ok(roles);
    }


    @Operation(summary = "" +
            "Delete or Restore User")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
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
            @ApiResponse(responseCode = "403", description = "forbidden",
                    content = {
                            @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))
                    }),
    })
    @PutMapping("/block-user")
    public ResponseEntity<Boolean> blockUser(@Validated @RequestBody UserStatusDto userStatusDto) {
        return ResponseEntity.ok(userService.blockUser(userStatusDto.getUserId(), userStatusDto.isStatus()));
    }

    @Operation(summary = "" +
            "Delete or Restore User")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
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
            @ApiResponse(responseCode = "403", description = "forbidden",
                    content = {
                            @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))
                    }),
    })
    @DeleteMapping("/delete-user")
    public ResponseEntity<Boolean> deleteUser(@Validated @RequestBody UserStatusDto userStatusDto) {
        return ResponseEntity.ok(userService.deleteUser(userStatusDto.getUserId(), userStatusDto.isStatus()));
    }

    @Operation(summary = "Create Account ")
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
                    }),
            @ApiResponse(responseCode = "403", description = "forbidden",
                    content = {
                            @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))
                    }),
    })

    @PostMapping("/create-user")
    public ResponseEntity<UserResponseDto> createUser(@Validated @RequestBody UserRequestDto userRequestDto, BindingResult result) {
        if (result.hasErrors()) {
            String errorMessage = result.getAllErrors()
                    .stream().map(DefaultMessageSourceResolvable::getDefaultMessage)
                    .collect(Collectors.joining(";"));

            throw new MissingFieldException(errorMessage);
        }
        User user = new User();
        Role roleUser = roleService.findByName(userRequestDto.getRole());
        user.setRoles(Collections.singletonList(roleUser));
        BeanUtils.copyProperties(userRequestDto, user);
        userService.save(user);
        UserResponseDto responseDto = new UserResponseDto();
        BeanUtils.copyProperties(user, responseDto);

        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }

    @Operation(summary = "" +
            "Update User")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
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
            @ApiResponse(responseCode = "403", description = "forbidden",
                    content = {
                            @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))
                    }),
    })
    @PostMapping("/update-user/{id}")
    public ResponseEntity<?> updateUser(@PathVariable(name = "id") UUID userId,
                                        @RequestBody UserUpdateDto userUpdateDto) {
        User updatedUser = userService.updateUser(userId, userUpdateDto);
        return ResponseEntity.ok(updatedUser);
    }

    @Operation(summary = "Get Total Deposit")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success",
                    content = {
                            @Content(mediaType = "application/json", schema = @Schema(implementation = Long.class))
                    }
            ),

            @ApiResponse(responseCode = "404", description = "type not found",
                    content = {
                            @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))
                    }),
            @ApiResponse(responseCode = "403", description = "forbidden",
                    content = {
                            @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))
                    }),

    })

    @GetMapping("/total-deposit")
    public ResponseEntity<?> getTotalDeposit(@RequestParam(name = "type", defaultValue = "date") String type) {
        if ("all".equals(type)) {
            return ResponseEntity.ok(userService.getTotalMoney(TransactionConstant.DEPOSIT_STATUS));
        }
        LocalDateTime[] dates = DateUtil.getDateBetween(type);
        Long totalMoney = userService.getTotalMoney(TransactionConstant.DEPOSIT_STATUS, dates[0], dates[1]);
        return ResponseEntity.ok(totalMoney != null ? totalMoney : 0);
    }

    @Operation(summary = "Count User")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success",
                    content = {
                            @Content(mediaType = "application/json", schema = @Schema(implementation = Long.class))
                    }
            ),

            @ApiResponse(responseCode = "404", description = "type not found",
                    content = {
                            @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))
                    }),
            @ApiResponse(responseCode = "403", description = "forbidden",
                    content = {
                            @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))
                    }),

    })
    @GetMapping("/count-user")
    public ResponseEntity<Long> countByDate(@RequestParam(name = "type", defaultValue = "date") String type) {
        if ("all".equals(type)) {
            return ResponseEntity.ok(userService.count());
        }
        LocalDateTime[] dates = DateUtil.getDateBetween(type);
        long count = userService.countUserBetweenDate(dates[0], dates[1]);
        return ResponseEntity.ok(count);
    }

    @Operation(summary = "Statistic Money")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success",
                    content = {
                            @Content(mediaType = "application/json", schema = @Schema(implementation = List.class))
                    }
            ),

            @ApiResponse(responseCode = "404", description = "type not found",
                    content = {
                            @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))
                    }),
            @ApiResponse(responseCode = "403", description = "forbidden",
                    content = {
                            @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))
                    }),

    })
    @GetMapping("/statistic-money")
    public ResponseEntity<?> statisticMoney(@RequestParam(name = "type", defaultValue = "week") String type) {
        return ResponseEntity.ok(userService.getStatisticMoney(TransactionConstant.DEPOSIT_STATUS, type));
    }

    @Operation(summary = "Statistic User ")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success",
                    content = {
                            @Content(mediaType = "application/json", schema = @Schema(implementation = List.class))
                    }
            ),

            @ApiResponse(responseCode = "404", description = "type not found",
                    content = {
                            @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))
                    }),
            @ApiResponse(responseCode = "403", description = "forbidden",
                    content = {
                            @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))
                    }),

    })
    @GetMapping("/statistic-user")
    public ResponseEntity<List<StatisticDto>> statisticUser(@RequestParam(name = "type", defaultValue = "week") String type) {
        return ResponseEntity.ok(userService.getStatisticUser(type));
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
            @ApiResponse(responseCode = "403", description = "forbidden",
                    content = {
                            @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))
                    }),
    })


    @GetMapping("/transactions")
    public ResponseEntity<DTOList<?>> findAllTransactions(@RequestParam(value = "id") UUID userId,
                                                          @RequestParam(value = "page", required = false, defaultValue = "1") int page,
                                                          @RequestParam(value = "size", required = false, defaultValue = "4") int size,
                                                          @RequestParam(name = "sort_field", required = false, defaultValue = "createdDate") String sortField,
                                                          @RequestParam(name = "sort_type", required = false, defaultValue = "desc") String sortType) {
        User user = new User();
        user.setId(userId);
        DTOList<?> transactions = userService.getTransactionManagementList(user, page, size, sortField, sortType);
        return ResponseEntity.ok(transactions);
    }


}
