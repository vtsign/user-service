package tech.vtsign.userservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.web.multipart.MultipartFile;
import tech.vtsign.userservice.domain.ResetLink;
import tech.vtsign.userservice.domain.User;
import tech.vtsign.userservice.model.*;
import tech.vtsign.userservice.model.zalopay.ZaloPayCallbackRequest;
import tech.vtsign.userservice.model.zalopay.ZaloPayResponse;

import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
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

    User updateAvatar(UUID id, MultipartFile file);

    Long maxReceivers(UUID id);

    boolean blockUser(UUID userUUID, boolean isBlock);

    boolean deleteUser(UUID userUUID, boolean isDelete);

    long countUserBetweenDate(LocalDateTime startDate, LocalDateTime endDate);

    boolean updateRoleUser(UUID userId, String Role);

    DTOList<?> getUserManagementList(int page, int pageSize, String sortField, String sortType, String keyword);

    Long getTotalMoney(String status, LocalDateTime fromDate, LocalDateTime toDate);
    Long getTotalMoney(String status);

    DTOList<?> getTransactionManagementList(User user, int page, int pageSize);

    List<StatisticDto> getStatisticMoney(String status, String type);

    List<StatisticDto> getStatisticUser(String type);

    DTOList<?> getBlockedUsers(int page, int pageSize, String sortField, String sortType, String keyword);

    DTOList<?> getDeletedUsers(int page, int pageSize, String sortField, String sortType, String keyword);

    void resetPassword(String email);

    ResetLink checkRestLink(UUID linkId);

    boolean resetPassword(UUID linkId, String password);
}
