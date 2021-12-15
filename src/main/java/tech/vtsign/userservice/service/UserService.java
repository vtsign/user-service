package tech.vtsign.userservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;
import tech.vtsign.userservice.domain.TransactionMoney;
import tech.vtsign.userservice.domain.User;
import tech.vtsign.userservice.model.UserChangePasswordDto;
import tech.vtsign.userservice.model.UserDepositDto;
import tech.vtsign.userservice.model.UserUpdateDto;
import tech.vtsign.userservice.model.zalopay.ZaloPayCallbackRequest;
import tech.vtsign.userservice.model.zalopay.ZaloPayResponse;

import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserService {
    User findByEmail(String email);

    List<User> findAll();

    <S extends User> S save(S s);

    User save2(User user);

    long count();

    Optional<User> login(String email, String password);

    User findById(UUID id);

    boolean activation(UUID id) throws NoSuchAlgorithmException;

    User getOrCreateUser(String email, String phone, String name) throws NoSuchAlgorithmException;

    User findUserById(UUID userUUID);

    User updateUser(UUID id, UserUpdateDto userUpdateDto);

    User changePassword(UUID id, UserChangePasswordDto userChangePasswordDto);

    ZaloPayResponse deposit(UUID id, UserDepositDto userDepositDto) throws JsonProcessingException;

    String updateUserBalance(ZaloPayCallbackRequest zaloPayCallbackRequest) throws JsonProcessingException;

    Boolean updateUserBalance(UUID userId, long amount, String status);

    Page<TransactionMoney> findAllTransactions(User user, int page, int size);

    User updateAvatar(UUID id, MultipartFile file);

    Long maxReceivers(UUID id);
}
