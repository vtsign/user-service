package tech.vtsign.userservice.service;

import tech.vtsign.userservice.domain.User;

import java.util.List;
import java.util.Optional;

public interface UserService {
    User findByEmail(String email);

    List<User> findAll();

    <S extends User> S save(S s);

    long count();

    Optional<User> login(String email, String password);
}
