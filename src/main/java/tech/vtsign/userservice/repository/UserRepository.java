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

    @Query("select u from User u where " +
            "(u.deleted = false) and " +
            "(u.blocked = false) and " +
            "(u.tempAccount = false) and " +
            "(u.email like %?1% " +
            "or concat(u.firstName, ' ', u.lastName) like %?1% " +
            "or u.phone like %?1% " +
            "or u.organization like %?1% " +
            "or u.address like %?1%)")
    Page<User> findAll(String keyword, Pageable pageable);

    @Query("select u from User u where u.deleted = false and u.blocked = false and u.tempAccount = false")
    Page<User> findAll(Pageable pageable);

    @Query("select u from User u where " +
            "(u.deleted = false) and " +
            "(u.blocked = true) and " +
            "(u.tempAccount = false ) and " +
            "(u.email like %?1% " +
            "or concat(u.firstName, ' ', u.lastName) like %?1% " +
            "or u.phone like %?1% " +
            "or u.organization like %?1% " +
            "or u.address like %?1%)")
    Page<User> findAllUserBlocked(String keyword, Pageable pageable);

    @Query("select u from User u where u.deleted = false and u.blocked = true and u.tempAccount = false ")
    Page<User> findAllUserBlocked(Pageable pageable);

    @Query("select u from User u where " +
            "(u.deleted = true) and " +
            "(u.tempAccount = false) and " +
            "(u.email like %?1% " +
            "or concat(u.firstName, ' ', u.lastName) like %?1% " +
            "or u.phone like %?1% " +
            "or u.organization like %?1% " +
            "or u.address like %?1%)")
    Page<User> findAllUserDeleted(String keyword, Pageable pageable);

    @Query("select u from User u where u.deleted = true and u.tempAccount = false ")
    Page<User> findAllUserDeleted(Pageable pageable);

    long countAllByCreatedDateBetweenAndTempAccount(LocalDateTime startDate, LocalDateTime endDate, boolean tempAccount);
}