package tech.vtsign.userservice;

import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.annotation.Rollback;
import tech.vtsign.userservice.domain.User;
import tech.vtsign.userservice.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;

@DataJpaTest(showSql = false)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Rollback(value = false)
@Slf4j
public class UserRepositoryTests {
    @Autowired
    private UserRepository repo;

    @Test
    public void createUser() {
        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
        User user = new User();
        String identity = "user";
        user.setEmail(identity + "@vtsign.tech");
        user.setPassword(bCryptPasswordEncoder.encode(identity));
        user.setFirstName(identity);
        user.setLastName(identity);
        user.setEnabled(true);
        User save = repo.save(user);
        Assertions.assertThat(save).isNotNull();
        log.info("Created user (email, password): ({}, {})", save.getEmail(), identity);

    }

    @Test
    public void createMultipleUsers() {
        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
        int numberOfUsers = 50;
        List<User> users = new ArrayList<>();
        for (int i = 1; i <= numberOfUsers; i++) {
            User user = new User();
            String identity = String.format("user%02d", i);
            user.setEmail(identity + "@vtsign.tech");
            user.setPassword(bCryptPasswordEncoder.encode(identity));
            user.setFirstName(identity);
            user.setLastName(identity);
            user.setEnabled(true);
            users.add(user);
            log.info("Created user (email, password): ({}, {})", user.getEmail(), identity);
        }
        repo.saveAll(users);
    }
}
