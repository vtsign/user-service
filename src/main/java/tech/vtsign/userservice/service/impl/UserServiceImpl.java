package tech.vtsign.userservice.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.vtsign.userservice.domain.User;
import tech.vtsign.userservice.exception.*;
import tech.vtsign.userservice.repository.UserRepository;
import tech.vtsign.userservice.service.AzureStorageService;
import tech.vtsign.userservice.service.UserProducer;
import tech.vtsign.userservice.service.UserService;
import tech.vtsign.userservice.utils.KeyGenerator;

import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final UserProducer userProducer;
    private final AzureStorageService azureStorageService;
    //    @Value("${spring.application.name}")
    private String TOPIC = "user_test";

    @Override
    public User findByEmail(String email) {
        Optional<User> opt = userRepository.findByEmail(email);
        return opt.orElseThrow(() -> new NotFoundException("User Not found"));
    }

    @Override
    public List<User> findAll() {
        return userRepository.findAll();
    }

    @Override
    public <S extends User> S save(S user) {
        Optional<User> opt = userRepository.findByEmail(user.getEmail());
        if (opt.isPresent()) {
            User oldUser = opt.get();
            if (oldUser.isTempAccount()) {
                user.setId(oldUser.getId());
                user.setPrivateKey(oldUser.getPrivateKey());
                user.setPublicKey(oldUser.getPublicKey());
            } else {
                throw new ConflictException("Email is already in use");
            }
        }

            user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
            User userSave = userRepository.save(user);
            userProducer.sendMessage(userSave);
            return (S) userSave;
    }

    @Override
    public long count() {
        return userRepository.count();
    }

    @Override
    public Optional<User> login(String email, String password) {

        Optional<User> opt = userRepository.findByEmail(email);

        if (opt.isPresent()) {
            User user = opt.get();

            if (user.isTempAccount() || !bCryptPasswordEncoder.matches(password, user.getPassword())) {
                throw new UnauthorizedException("Invalid Email or Password");
            }

            if (!user.isEnabled()) {
                throw new LockedException("User haven't enabled yet");
            }

            return Optional.of(user);
        }
        throw new UnauthorizedException("Invalid Email or Password");

    }

    @Override
    public User findById(UUID id) {
        Optional<User> opt = userRepository.findById(id);
        return opt.orElseThrow(() -> new BadRequestException("Link active not exist"));
    }

    @Override
    @Transactional
    public boolean activation(UUID id) throws NoSuchAlgorithmException {
        User user = findById(id);
        if (user.isTempAccount()) {
            user.setEnabled(true);
            user.setTempAccount(false);
            return true;
        }
        if (user.isEnabled()) {
            return false;
        }
        user.setEnabled(true);
        createUserKeyPair(id, user);
        return true;
    }

    private void createUserKeyPair(UUID id, User user) throws NoSuchAlgorithmException {
        KeyGenerator keyGenerator = new KeyGenerator(2048);
        PrivateKey privateKey = keyGenerator.getPrivateKey();
        PublicKey publicKey = keyGenerator.getPublicKey();
        user.setPrivateKey(azureStorageService.uploadNotOverride(String.format("%s/%s", id, UUID.randomUUID()), privateKey.getEncoded()));
        user.setPublicKey(azureStorageService.uploadNotOverride(String.format("%s/%s", id, UUID.randomUUID()), publicKey.getEncoded()));
    }

    @Override
    public User getOrCreateUser(String email, String name) throws NoSuchAlgorithmException {
        Optional<User> opt = userRepository.findByEmail(email);
        User user = opt.orElse(null);

        if (user == null) {
            user = new User();
            UUID userUUID = UUID.randomUUID();
            user.setId(userUUID);
            user.setEmail(email);
            user.setLastName(name);
            user.setEnabled(false);
            user.setTempAccount(true);
            createUserKeyPair(userUUID, user);
            userRepository.save(user);
        }

        return user;
    }

}