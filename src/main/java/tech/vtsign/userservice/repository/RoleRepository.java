package tech.vtsign.userservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tech.vtsign.userservice.domain.Role;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
}
