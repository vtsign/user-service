package tech.vtsign.userservice.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import tech.vtsign.userservice.domain.Signature;
import tech.vtsign.userservice.domain.User;
import tech.vtsign.userservice.exception.*;
import tech.vtsign.userservice.repository.UserRepository;
import tech.vtsign.userservice.service.AzureStorageService;
import tech.vtsign.userservice.service.UserProducer;
import tech.vtsign.userservice.service.UserService;
import tech.vtsign.userservice.utils.TextToGraphics;

import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
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

    @Override
    public User findByEmail(String email) {
        Optional<User> opt = userRepository.findByEmail(email);
        return opt.orElseThrow(() -> new NotFoundException("User Not found"));
    }

    @Override
    public User findUserById(UUID uuid) {
        Optional<User> opt = userRepository.findById(uuid);
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
                user.setSignatures(oldUser.getSignatures());
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
        createUserSignature(id, user);
        return true;
    }

    private void createUserSignature(UUID id, User user) throws NoSuchAlgorithmException {

        List<Signature> signatures = new ArrayList<>();
        Signature signature1 = new Signature();
        Signature signature2 = new Signature();
        byte[] signatureImage1 = TextToGraphics.generateSignatureStyle1(user.getFullName());
        byte[] signatureImage2 = TextToGraphics.generateSignatureStyle2(user.getFullName());
        signature1.setUrl(azureStorageService.uploadNotOverride(String.format("%s/%s.png", id, UUID.randomUUID()), signatureImage1));
        signature2.setUrl(azureStorageService.uploadNotOverride(String.format("%s/%s.png", id, UUID.randomUUID()), signatureImage2));
        signatures.add(signature1);
        signatures.add(signature2);
        user.setSignatures(signatures);
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
            createUserSignature(userUUID, user);
            userRepository.save(user);
        }

        return user;
    }

    @SneakyThrows
    @Override
    public List<Signature> saveSignature(User user, MultipartFile signatureImage, String type) {
        // SignatureType...
        // client -> gateway -> auth -> user -> auth -> gateway -> user
        azureStorageService.uploadNotOverride(String.format("%s/%s.png", user.getId(), UUID.randomUUID()), signatureImage.getBytes());
        return null;
    }

}