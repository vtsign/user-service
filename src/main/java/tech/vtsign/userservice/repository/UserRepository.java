package tech.vtsign.userservice.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import tech.vtsign.userservice.domain.User;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);

    @Query("select u from User u where u.email like %?1% " +
            "or concat(u.firstName, ' ', u.lastName) like %?1% " +
            "or u.phone like %?1% " +
            "or u.organization like %?1% " +
            "or u.address like %?1%")
    Page<User> findAll(String keyword, Pageable pageable);

    long countAllByCreatedDateBetween(LocalDateTime startDate, LocalDateTime endDate);
}