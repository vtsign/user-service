package tech.vtsign.userservice.controller;

import org.springframework.http.HttpRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
public class UserController {
    @GetMapping("/welcome")
    public ResponseEntity<String> welcome(@RequestHeader("Authorization") String language){

        return ResponseEntity.ok(language);
    }
}
