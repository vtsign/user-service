package tech.vtsign.userservice.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.vtsign.userservice.domain.User;
import tech.vtsign.userservice.exception.BadRequest;
import tech.vtsign.userservice.exception.user.UserInvalidEmailOrPassword;
import tech.vtsign.userservice.exception.user.UserNotFoundException;
import tech.vtsign.userservice.repository.UserRepository;
import tech.vtsign.userservice.service.UserProducer;
import tech.vtsign.userservice.service.UserService;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final UserProducer userProducer;
//    @Value("${spring.application.name}")
    private String TOPIC="user_test";

    @Override
    public User findByEmail(String email) {
        Optional<User> opt = userRepository.findByEmail(email);
        return opt.orElseThrow(() -> new UserNotFoundException("User Not found"));
    }

    @Override
    public List<User> findAll() {
        return userRepository.findAll();
    }

    @Override
    public <S extends User> S save(S user) {
        Optional<User> byEmail = userRepository.findByEmail(user.getEmail());
        if (byEmail.isPresent())
            throw new BadRequest("Email is already in use");
        user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
        User userSave = userRepository.save(user);
        userProducer.sendMessage(TOPIC, userSave);
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
            if (bCryptPasswordEncoder.matches(password, user.getPassword())) {
                return Optional.of(user);
            }
        }
        throw new UserInvalidEmailOrPassword("Invalid Email or Password");

    }
    @Override
    public User findById(UUID id){
       Optional<User> opt = userRepository.findById(id);
       return opt.orElseThrow(() -> new UserNotFoundException("User Not Found"));
    }

    @Override
    @Transactional
    public boolean activation(UUID id) {
        User user = findById(id);
        user.setEnabled(true);
        return true;
    }


}