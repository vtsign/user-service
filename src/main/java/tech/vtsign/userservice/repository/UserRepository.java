package tech.vtsign.userservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tech.vtsign.userservice.domain.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
}
