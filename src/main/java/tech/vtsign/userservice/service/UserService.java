package tech.vtsign.userservice.service;

import lombok.SneakyThrows;
import org.springframework.web.multipart.MultipartFile;
import tech.vtsign.userservice.domain.Signature;
import tech.vtsign.userservice.domain.User;

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

    User getOrCreateUser(String email, String name) throws NoSuchAlgorithmException;

    User findUserById(UUID userUUID);

    @SneakyThrows
    List<Signature> saveSignature(User user, MultipartFile signatureImage, String type);
}
