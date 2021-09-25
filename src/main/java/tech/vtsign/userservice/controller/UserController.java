package tech.vtsign.userservice.controller;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class UserController {

    @PostMapping("/register")
    public void register() {

    }

    @PostMapping("/login")
    public void register(String username, String password) {

    }

    @GetMapping("/")
    public String test(@RequestHeader("Authorization") String token) {
        return token;
    }
}
