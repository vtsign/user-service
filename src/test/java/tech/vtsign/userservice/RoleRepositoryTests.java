package tech.vtsign.userservice;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Profile;
import org.springframework.test.annotation.Rollback;
import tech.vtsign.userservice.domain.Role;
import tech.vtsign.userservice.repository.RoleRepository;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@DataJpaTest(showSql = false)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Rollback(value = false)
@Slf4j
@Profile("dev")
public class RoleRepositoryTests {
    @Autowired
    private RoleRepository roleRepository;

    @Test
    public void testCreateRole() {
        log.info("testCreateRole");
        Role admin = new Role();
        admin.setName("ADMIN");
        roleRepository.save(admin);
        assertThat(admin.getId()).isNotNull();
    }
}
