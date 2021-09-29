package tech.vtsign.userservice.controller;


import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tech.vtsign.userservice.domain.User;
import tech.vtsign.userservice.model.UserLoginDto;
import tech.vtsign.userservice.model.UserRequestDto;
import tech.vtsign.userservice.model.UserResponseDto;
import tech.vtsign.userservice.service.UserService;

import java.util.Optional;

@RequiredArgsConstructor
@RestController
public class UserController {

    private final UserService userService;

    @GetMapping("/{email}")
    public ResponseEntity<UserResponseDto> retrieveUser(@PathVariable String email) {
        User user = userService.findByEmail(email);
        UserResponseDto userRes = new UserResponseDto();
        BeanUtils.copyProperties(user, userRes);
        return ResponseEntity.ok().body(userRes);
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponseDto> register(@RequestBody UserRequestDto userRequestDto) {
        User user = new User();
        BeanUtils.copyProperties(userRequestDto, user);
        userService.save(user);
        UserResponseDto responseDto = new UserResponseDto();
        BeanUtils.copyProperties(user, responseDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }

    @PostMapping("/login")
    public ResponseEntity<UserResponseDto> login(@RequestBody UserLoginDto userLoginDto) {
        Optional<User> opt = userService.login(userLoginDto.getEmail(), userLoginDto.getPassword());
        UserResponseDto userResponseDto = new UserResponseDto();
        BeanUtils.copyProperties(opt.get(), userResponseDto);
        return ResponseEntity.ok().body(userResponseDto);
    }
}
