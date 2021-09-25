package tech.vtsign.userservice.controller;


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
