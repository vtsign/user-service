package tech.vtsign.userservice.service;

import tech.vtsign.userservice.domain.User;
import tech.vtsign.userservice.model.UserChangePasswordDto;
import tech.vtsign.userservice.model.UserUpdateDto;

import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserService {
    User findByEmail(String email);

    List<User> findAll();

    <S extends User> S save(S s);

    long count();

    Optional<User> login(String email, String password);

    User findById(UUID id);

    boolean activation(UUID id) throws NoSuchAlgorithmException;

    User getOrCreateUser(String email, String phone, String name) throws NoSuchAlgorithmException;

    User findUserById(UUID userUUID);

    User updateUser(UUID id, UserUpdateDto userUpdateDto);

    User changePassword(UUID id, UserChangePasswordDto userChangePasswordDto);
}
