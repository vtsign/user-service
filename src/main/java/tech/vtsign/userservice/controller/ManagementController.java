package tech.vtsign.userservice.controller;

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

    @GetMapping("/list")
    public DTOList<?> getManagement(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "pageSize", defaultValue = "5") int pageSize,
            @RequestParam(name = "sortField", required = false, defaultValue = "firstName") String sortField,
            @RequestParam(name = "sortType", required = false, defaultValue = "asc") String sortType,
            @RequestParam(name = "keyword", required = false, defaultValue = "") String keyword
    ) {
        return userService.getUserManagementList(page, pageSize, sortField, sortType, keyword);
    }

    @GetMapping("/roles")
    public ResponseEntity<?> retrieveAllRoles() {
        List<Role> roles = roleService.findAll();
        return ResponseEntity.ok(roles);
    }

    @PutMapping("/block-user")
    public ResponseEntity<Boolean> blockUser(@RequestBody UserStatusDto userStatusDto) {
        return ResponseEntity.ok(userService.blockUser(userStatusDto.getUserId(), userStatusDto.isStatus()));
    }

    @DeleteMapping("/delete-user")
    public ResponseEntity<Boolean> deleteUser(@RequestBody UserStatusDto userStatusDto) {
        return ResponseEntity.ok(userService.deleteUser(userStatusDto.getUserId(), userStatusDto.isStatus()));
    }


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

    @PostMapping("/update-user/{id}")
    public ResponseEntity<?> updateUser(@PathVariable(name = "id") UUID userId,
                                        @RequestBody UserUpdateDto userUpdateDto) {
        User updatedUser = userService.updateUser(userId, userUpdateDto);
        return ResponseEntity.ok(updatedUser);
    }

    @GetMapping("/total-deposit")
    public ResponseEntity<?> getTotalDeposit(@RequestParam(value = "type", defaultValue = "date") String type) {
        List<LocalDateTime> dates = DateUtil.getDateBetween(type);
        Long totalMoney = userService.getTotalMoney(TransactionConstant.DEPOSIT_STATUS, dates.get(0), dates.get(1));
        return ResponseEntity.ok(totalMoney != null ? totalMoney : 0);
    }

    @GetMapping("/count-user")
    public ResponseEntity<?> countByDate(@RequestParam(value = "type", defaultValue = "date") String type) {
        List<LocalDateTime> dates = DateUtil.getDateBetween(type);
        long count = userService.countUserBetweenDate(dates.get(0), dates.get(1));
        return ResponseEntity.ok(count);
    }

}
