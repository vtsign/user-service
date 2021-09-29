package tech.vtsign.userservice.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import tech.vtsign.userservice.domain.Role;
import tech.vtsign.userservice.domain.User;
import tech.vtsign.userservice.exception.SaveError;
import tech.vtsign.userservice.exception.user.UserInvalidEmailOrPassword;
import tech.vtsign.userservice.exception.user.UserNotFoundException;
import tech.vtsign.userservice.repository.UserRepository;
import tech.vtsign.userservice.service.UserService;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

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
        try {
            List<Role> roles = List.of(Role.builder().name("USER").build());
            user.setRoles(roles);
            user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
            return userRepository.save(user);
        } catch (Exception e) {
            throw new SaveError("Save Fail");
        }
    }

    @Override
    public long count() {
        return userRepository.count();
    }

    @Override
    public Optional<User> login(String email, String password) {
        Optional<User> opt = userRepository.findByEmail(email);
        User user = opt.orElseThrow(() -> new UserInvalidEmailOrPassword("Invalid Email or Password"));
        if (bCryptPasswordEncoder.matches(password, user.getPassword())) {
            return Optional.of(user);
        }
        return Optional.empty();
    }
}
