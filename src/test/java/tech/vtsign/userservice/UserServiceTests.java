package tech.vtsign.userservice;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import tech.vtsign.userservice.domain.User;
import tech.vtsign.userservice.service.UserService;

import java.util.UUID;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class UserServiceTests {
    @Autowired
    private UserService userService;

    @Test
    void findUserById() {
        User user = userService.findByEmail("rrfpb.tuan1@inbox.testmail.app");
        System.out.println(user.getId());
        System.out.println(UUID.fromString("03948812-c828-49b7-b07e-0630544a20ea"));
        System.out.println(UUID.fromString("03948812-c828-49b7-b07e-0630544a20ea").equals(user.getId()));
        userService.findAll().forEach(u -> {
            System.out.println(u.getId());
            System.out.println(u.getEmail());
        });
        User findUser = userService.findUserById(UUID.fromString("03948812-c828-49b7-b07e-0630544a20ea"));
        Assertions.assertThat(findUser).isNotNull();
    }
}
