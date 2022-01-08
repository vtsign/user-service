package tech.vtsign.userservice.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import tech.vtsign.userservice.constant.TransactionConstant;
import tech.vtsign.userservice.domain.ResetLink;
import tech.vtsign.userservice.domain.Role;
import tech.vtsign.userservice.domain.TransactionMoney;
import tech.vtsign.userservice.domain.User;
import tech.vtsign.userservice.exception.*;
import tech.vtsign.userservice.model.*;
import tech.vtsign.userservice.model.zalopay.*;
import tech.vtsign.userservice.proxy.DocumentServiceProxy;
import tech.vtsign.userservice.proxy.ZaloPayServiceProxy;
import tech.vtsign.userservice.repository.ResetLinkRepository;
import tech.vtsign.userservice.repository.RoleRepository;
import tech.vtsign.userservice.repository.TransactionMoneyRepository;
import tech.vtsign.userservice.repository.UserRepository;
import tech.vtsign.userservice.service.AzureStorageService;
import tech.vtsign.userservice.service.RoleService;
import tech.vtsign.userservice.service.UserProducer;
import tech.vtsign.userservice.service.UserService;
import tech.vtsign.userservice.utils.DateUtil;
import tech.vtsign.userservice.utils.zalopay.crypto.HMACUtil;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.*;

import static tech.vtsign.userservice.utils.DateUtil.getCurrentTimeString;

@RequiredArgsConstructor
@Service
@Slf4j
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final ResetLinkRepository resetLinkRepository;
    private final RoleService roleService;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final UserProducer userProducer;
    private final ZaloPayServiceProxy zaloPayServiceProxy;
    private final DocumentServiceProxy documentServiceProxy;
    private final TransactionMoneyRepository transactionMoneyRepository;
    private final AzureStorageService azureStorageService;

    @Value("${tech.vtsign.kafka.user-service.register}")
    private String topicUserServiceRegister;
    @Value("${tech.vtsign.kafka.user-service.reset-password}")
    private String topicUserServiceResetPassword;
    @Value("${tech.vtsign.kafka.user-service.notify-common}")
    private String topicUserServiceNotifyCommon;

    @Value("${tech.vtsign.zalopay.app-id}")
    private int appId;
    @Value("${tech.vtsign.zalopay.app-user}")
    private String appUser;
    @Value("${tech.vtsign.zalopay.redirect-url}")
    private String redirectUrl;
    @Value("${tech.vtsign.zalopay.callback-url}")
    private String callbackUrl;
    @Value("${tech.vtsign.zalopay.mac-key}")
    private String macKey;
    private static String callbackKey;
    @Value("${tech.vtsign.hostname}")
    private String hostname;
    @Value("${tech.vtsign.zalopay.amount}")
    private long amount;

    @Value("${tech.vtsign.zalopay.init-balance}")
    private long initBalance;

    @Value("${tech.vtsign.zalopay.callback-key}")
    public void setCallbackKey(String key) {
        UserServiceImpl.callbackKey = key;
    }

    private static Mac HmacSHA256;

    private static Mac getMac() {
        if (HmacSHA256 == null) {
            try {
                HmacSHA256 = Mac.getInstance("HmacSHA256");
                HmacSHA256.init(new SecretKeySpec(callbackKey.getBytes(), "HmacSHA256"));
            } catch (NoSuchAlgorithmException | InvalidKeyException e) {
                e.printStackTrace();
            }
        }
        return HmacSHA256;
    }

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
    public User updateUser(UUID id, UserUpdateDto userUpdateDto) {
        User user = findUserById(id);
        BeanUtils.copyProperties(userUpdateDto, user);
        if (userUpdateDto.getRole() != null) {
            Role roleUser = roleService.findByName(userUpdateDto.getRole());
            List<Role> roles = new ArrayList<>();
            roles.add(roleUser);
            user.setRoles(roles);
        }
        User userSave = userRepository.save(user);
        documentServiceProxy.updateUser(userSave);
        // sent message
        CommonMessage commonMessage = new CommonMessage();
        commonMessage.setTitle("Cập nhật thông tin tài khoản");
        commonMessage.setMessage("Tài khoản của bạn vừa được cập nhập thông tin thành công");
        commonMessage.setTo(user.getEmail());
        sendNotificationMessage(commonMessage);
        return userSave;
    }

    @Override
    public User changePassword(UUID id, UserChangePasswordDto userChangePasswordDto) {
        User user = findUserById(id);
        if (!bCryptPasswordEncoder.matches(userChangePasswordDto.getOldPassword(), user.getPassword())) {
            throw new UnauthorizedException("Invalid old password");
        }
        user.setPassword(bCryptPasswordEncoder.encode(userChangePasswordDto.getNewPassword()));
        User saved = userRepository.save(user);
        // sent message
        CommonMessage commonMessage = new CommonMessage();
        commonMessage.setTitle("Cập nhật mật khẩu");
        commonMessage.setMessage("Mật khẩu của bạn vừa được thay đổi thành công");
        commonMessage.setTo(user.getEmail());
        sendNotificationMessage(commonMessage);

        return saved;

    }

    @Override
    public ZaloPayResponse deposit(UUID id, UserDepositDto userDepositDto) throws JsonProcessingException {
        UUID orderId = UUID.randomUUID();
        String type = userDepositDto.getMethod();
        long amount = userDepositDto.getAmount();

        List<Item> items = List.of(new Item(orderId, id, userDepositDto.getAmount(), type));
        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        String key1 = macKey;

        OAOrder oaOrder = new OAOrder();
        oaOrder.setAppId(appId);
        oaOrder.setAppTransId(getCurrentTimeString("yyMMdd") + "_" + new Random().nextInt(100000));
        oaOrder.setAppTime(new Date().getTime());
        oaOrder.setAppUser(appUser);
        oaOrder.setAmount(amount);
        oaOrder.setDescription("VTSign Order ID " + orderId);
        if (type.equals("ATM")) {
            oaOrder.setBankCode("");
            oaOrder.setEmbedData("{\"bankgroup\":\"ATM\"}");
        } else {
            oaOrder.setBankCode(type);
            oaOrder.setEmbedData("{}");
        }
        oaOrder.setRedirectUrl(redirectUrl);
        oaOrder.setCallbackUrl(callbackUrl);
        oaOrder.setItem(ow.writeValueAsString(items));


        // app_id +”|”+ app_trans_id +”|”+ appuser +”|”+ amount +"|" + app_time +”|”+ embed_data +"|" +item
        String data = oaOrder.getAppId() + "|" + oaOrder.getAppTransId() + "|" + oaOrder.getAppUser() + "|" + oaOrder.getAmount()
                + "|" + oaOrder.getAppTime() + "|" + oaOrder.getEmbedData() + "|" + oaOrder.getItem();
        oaOrder.setMac(HMACUtil.HMacHexStringEncode(HMACUtil.HMACSHA256, key1, data));

        return zaloPayServiceProxy.createOrder(oaOrder);
    }

    @Override
    @Transactional
    public String updateUserBalance(ZaloPayCallbackRequest zaloPayCallbackRequest) throws JsonProcessingException {
        String dataStr = zaloPayCallbackRequest.getData();
        String reqMac = zaloPayCallbackRequest.getMac();
        Mac macH256 = getMac();
        byte[] hashBytes = macH256.doFinal(dataStr.getBytes());
        String mac = DatatypeConverter.printHexBinary(hashBytes).toLowerCase();
        ZaloPayCallBackResponse response = new ZaloPayCallBackResponse();
        // kiểm tra callback hợp lệ (đến từ ZaloPay server)
        if (!reqMac.equals(mac)) {
            // callback không hợp lệ
            log.error("callback không hợp lệ");
            response.setCode(-1);
            response.setMessage("mac not equal");
        } else {
            // thanh toán thành công
            // merchant cập nhật trạng thái cho đơn hàng
            ObjectMapper m = new ObjectMapper();
            DataCallBack dataCallBack = null;
            try {
                dataCallBack = m.readValue(dataStr, DataCallBack.class);
                log.info("pay success, callback data: {}", dataCallBack);
                List<Item> items = m.readValue(dataCallBack.getItem(),
                        m.getTypeFactory().constructCollectionType(List.class, Item.class));
                Item item = items.get(0);
                User user = findById(item.getUserId());
                if (user == null) {
                    log.error("user null");
                    response.setCode(-1);
                } else {
                    log.info("update user balance from {} to {}", user.getBalance(), user.getBalance() + item.getAmount());
                    user.setBalance(user.getBalance() + item.getAmount());
                    TransactionMoney transactionMoney = new TransactionMoney();
                    transactionMoney.setAmount(item.getAmount());
                    transactionMoney.setMethod(item.getMethod());
                    transactionMoney.setStatus(TransactionConstant.DEPOSIT_STATUS);
                    transactionMoney.setDescription(TransactionConstant.DEPOSIT_DESCRIPTION);
                    transactionMoney.setUser(user);
                    transactionMoneyRepository.save(transactionMoney);
                    // sent message
                    CommonMessage commonMessage = new CommonMessage();
                    commonMessage.setTitle("Nạp tiền thành công");
                    commonMessage.setMessage("Bạn vừa nạp thành công " + item.getAmount() + " VND");
                    commonMessage.setTo(user.getEmail());
                    sendNotificationMessage(commonMessage);
                }
                response.setCode(1);
                response.setMessage("success");
            } catch (JsonProcessingException ex) {
                response.setCode(0);
                response.setMessage(ex.getMessage());
                ex.printStackTrace();
            }
        }
        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        return ow.writeValueAsString(response);
    }

    @Override
    @Transactional
    public Boolean updateUserBalance(UUID userId, long amount, String status) {
        User user = this.findUserById(userId);

        TransactionMoney transactionMoney = new TransactionMoney();
        transactionMoney.setAmount(amount);
        transactionMoney.setStatus(status);
        transactionMoney.setUser(user);
        if (status.equals(TransactionConstant.PAYMENT_STATUS)) {
            if (user.getBalance() < amount) {
                return false;
            }
            transactionMoney.setMethod(TransactionConstant.PAYMENT);
            transactionMoney.setDescription(TransactionConstant.PAYMENT_DESCRIPTION);
            transactionMoney.setUser(user);
            user.getTransactionMonies().add(transactionMoney);
            user.setBalance(user.getBalance() - amount);

        }
        return true;
    }

    @Override
    public User updateAvatar(UUID id, MultipartFile file) {
        User user = findUserById(id);
        String fileName = file.getOriginalFilename();
        try {
            String avatar = azureStorageService.uploadOverride(String.format("%s/%s", user.getId(), fileName),
                    file.getBytes());
            user.setAvatar(avatar);
        } catch (IOException e) {
            e.printStackTrace();
            throw new BadRequestException("Upload file error");
        }
        return userRepository.save(user);
    }

    @Override
    public Long maxReceivers(UUID id) {
        Optional<User> opt = userRepository.findById(id);
        User user = opt.orElseThrow(() -> new NotFoundException("User not found"));
        return user.getBalance() / amount;
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
            } else {
                throw new ConflictException("Email is already in use");
            }
        }
        user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
        User userSave = userRepository.save(user);
        Activation activation = new Activation();
        activation.setTo(user.getEmail());
        activation.setUrl(String.format("%s/activation/%s", hostname, user.getId()));
        userProducer.sendMessage(activation, topicUserServiceRegister);
        return (S) userSave;
    }

    @Override
    public User save2(User user) {
        Optional<User> opt = userRepository.findByEmail(user.getEmail());
        if (opt.isPresent()) {
            User oldUser = opt.get();
            if (oldUser.isTempAccount()) {
                user.setId(oldUser.getId());
            } else {
                throw new ConflictException("Email is already in use");
            }
        }

        user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
        User userSave = userRepository.save(user);
        Activation activation = new Activation();
        activation.setTo(user.getEmail());
        activation.setUrl(String.format("%s/activation/%s", "https://qlda02.herokuapp.com", user.getId()));
        userProducer.sendMessage(activation, topicUserServiceRegister);
        return userSave;
    }

    @Override
    public long count() {
        return userRepository.countAllByTempAccount(false);
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

            if (user.isBlocked()) {
                throw new LockedException("User is blocked. Please contact admin");
            }

            if (user.isDeleted()) {
                throw new LockedException("User is deleted. Please contact admin");
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
    public boolean activation(UUID id) {
        User user = findById(id);
        if (user.isTempAccount()) {
            user.setEnabled(true);
            user.setTempAccount(false);
            return true;
        }
        if (user.isEnabled()) {
            return false;
        }

        TransactionMoney transactionMoney = new TransactionMoney();
        transactionMoney.setAmount(initBalance);
        transactionMoney.setMethod(TransactionConstant.INIT);
        transactionMoney.setStatus(TransactionConstant.INIT_BALANCE);
        transactionMoney.setDescription(TransactionConstant.INIT_DESCRIPTION);
        transactionMoney.setUser(user);
        transactionMoneyRepository.save(transactionMoney);
        user.setBalance(initBalance);
        user.setEnabled(true);
        return true;
    }

    @Override
    public User getOrCreateUser(String email, String phone, String name) {
        Optional<User> opt = userRepository.findByEmail(email);
        User user = opt.orElse(null);

        if (user == null) {
            user = new User();
            user.setEmail(email);
            user.setPhone(phone);
            String[] names = name.split(" ");
            String firstName = names[0];
            String lastName = "";
            if (names.length > 1) {
                lastName = name.substring(name.indexOf(" ") + 1);
            }
            user.setFirstName(firstName);
            user.setLastName(lastName);
            user.setEnabled(false);
            user.setTempAccount(true);
            userRepository.save(user);
        }

        return user;
    }

    @Transactional
    @Override
    public boolean blockUser(UUID userUUID, boolean isBlock) {
        User user = findById(userUUID);
        user.setBlocked(isBlock);
        // sent message
        String message = "";
        CommonMessage commonMessage = new CommonMessage();
        commonMessage.setTitle("Khóa tài khoản");
        message = isBlock ? "Tài khoản của bạn đã bị khóa" : "Tài khoản của bạn đã được mở khóa";
        commonMessage.setMessage(message);
        commonMessage.setTo(user.getEmail());
        sendNotificationMessage(commonMessage);
        return true;
    }

    @Transactional
    @Override
    public boolean deleteUser(UUID userUUID, boolean isDelete) {
        User user = findById(userUUID);
        user.setDeleted(isDelete);
        // sent message
        String message = "";
        CommonMessage commonMessage = new CommonMessage();
        commonMessage.setTitle("Xóa tài khoản");
        message = isDelete ? "Tài khoản của bạn đã bị vô hiệu hóa" : "Tài khoản đã khôi phục lại như ban đầu";
        commonMessage.setMessage(message);
        commonMessage.setTo(user.getEmail());
        sendNotificationMessage(commonMessage);
        return true;
    }

    @Override
    public long countUserBetweenDate(LocalDateTime from, LocalDateTime to) {
        return userRepository.countAllByCreatedDateBetweenAndTempAccount(from, to, false);
    }


    @Override
    @Transactional
    public boolean updateRoleUser(UUID userId, String nameRole) {
        User user = findUserById(userId);
        Optional<Role> roleOpt = roleRepository.findByName(nameRole);
        Role role = roleOpt.orElseThrow(() -> new NotFoundException("Role not found"));
        user.setRoles(List.of(role));
        return true;
    }

    @Override
    public DTOList<?> getUserManagementList(int page, int pageSize, String sortField, String sortType, String keyword) {
        Pageable pageable = getPageable(page, pageSize, sortField, sortType);

        Page<User> userPage;
        if (keyword != null && !keyword.isEmpty()) {
            userPage = userRepository.findAll(keyword, pageable);
        } else {
            userPage = userRepository.findAll(pageable);
        }
        return getDtoList(page, userPage);
    }

    @Override
    public DTOList<?> getBlockedUsers(int page, int pageSize, String sortField, String sortType, String keyword) {
        Pageable pageable = getPageable(page, pageSize, sortField, sortType);

        Page<User> userPage;
        if (keyword != null && !keyword.isEmpty()) {
            userPage = userRepository.findAllUserBlocked(keyword, pageable);
        } else {
            userPage = userRepository.findAllUserBlocked(pageable);
        }

        return getDtoList(page, userPage);
    }

    @Override
    public DTOList<?> getDeletedUsers(int page, int pageSize, String sortField, String sortType, String keyword) {
        Pageable pageable = getPageable(page, pageSize, sortField, sortType);

        Page<User> userPage;
        if (keyword != null && !keyword.isEmpty()) {
            userPage = userRepository.findAllUserDeleted(keyword, pageable);
        } else {
            userPage = userRepository.findAllUserDeleted(pageable);
        }

        return getDtoList(page, userPage);
    }

    @Override
    public void resetPassword(String email) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new NotFoundException("User not found"));
        if (user.isTempAccount()) {
            throw new NotFoundException("User not found");
        }
        ResetLink resetLink = new ResetLink();
        resetLink.setUser(user);
        resetLink.setCreatedAt(LocalDateTime.now());
        resetLink.setExpiresAt(LocalDateTime.now().plusHours(1));
        ResetLink resetLinkSave = resetLinkRepository.save(resetLink);
        // send to notification service
        ResetPasswordTransfer resetPasswordTransfer = new ResetPasswordTransfer();
        resetPasswordTransfer.setUrl(String.format("%s/reset-password?code=%s", hostname, resetLinkSave.getId()));
        resetPasswordTransfer.setTo(user.getEmail());
        resetPasswordTransfer.setExpireAt(resetLink.getExpiresAt());
        userProducer.sendMessage(resetPasswordTransfer, topicUserServiceResetPassword);
    }

    @Override
    public ResetLink checkRestLink(UUID linkId) {
        ResetLink resetLink = resetLinkRepository.findById(linkId)
                .orElseThrow(() -> new NotFoundException("Reset link not found"));
        if (resetLink.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Reset link expired");
        }
        return resetLink;
    }

    @Override
    public boolean resetPassword(UUID linkId, String password) {
        ResetLink resetLink = checkRestLink(linkId);
        User user = resetLink.getUser();
        user.setPassword(bCryptPasswordEncoder.encode(password));
        userRepository.save(user);
        resetLinkRepository.delete(resetLink);
        // sent message
        CommonMessage commonMessage = new CommonMessage();
        commonMessage.setTitle("Cập nhật mật khẩu thành công");
        commonMessage.setMessage("Mật khẩu của bạn đã được cập nhật thành công. Vui lòng vào " + hostname + "/login để đăng nhập lại.");
        commonMessage.setTo(user.getEmail());
        return true;
    }

    private Pageable getPageable(int page, int pageSize, String sortField, String sortType) {
        Sort sort = Sort.by(sortField).ascending();
        if (sortType.equals("desc")) {
            sort = Sort.by(sortField).descending();
        }
        return PageRequest.of(page - 1, pageSize, sort);
    }

    private DTOList<?> getDtoList(int page, Page<User> userPage) {
        DTOList<UserResponseDto> DTOList = new DTOList<>();
        DTOList.setPage(page);
        DTOList.setPageSize(userPage.getSize());
        DTOList.setTotalElements(userPage.getTotalElements());
        DTOList.setTotalPages(userPage.getTotalPages());
        DTOList.setList(convertToDto(userPage.getContent(), UserResponseDto.class));
        return DTOList;
    }

    private <T, V> List<T> convertToDto(List<V> source, Class<T> clazz) {
        List<T> des = new ArrayList<>();
        for (V v : source) {
            T t = BeanUtils.instantiateClass(clazz);
            BeanUtils.copyProperties(v, t);
            des.add(t);
        }
        return des;
    }

    @Override
    public Long getTotalMoney(String status, LocalDateTime fromDate, LocalDateTime toDate) {
        return transactionMoneyRepository.getSumAmountByStatus(status, fromDate, toDate);
    }

    @Override
    public Long getTotalMoney(String status) {
        return transactionMoneyRepository.getSumAmountByStatus(status);
    }

    @Override
    public DTOList<?> getTransactionManagementList(User user, int page, int pageSize, String sortField, String sortType) {
        Sort sort = Sort.by(sortField).ascending();
        if (sortType.equals("desc")) {
            sort = Sort.by(sortField).descending();
        }
        Pageable pageable = PageRequest.of(page - 1, pageSize, sort);
        Page<TransactionMoney> transactionMoneyPage = transactionMoneyRepository.findAll(new Specification<>() {
            final List<Predicate> predicates = new ArrayList<>();

            @Override
            public Predicate toPredicate(Root<TransactionMoney> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
                if (user != null) {
                    predicates.add(criteriaBuilder.and(criteriaBuilder.equal(root.get("user"), user)));
                }
                return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
            }
        }, pageable);
        DTOList<TransactionMoneyDto> DTOList = new DTOList<>();
        DTOList.setPage(page);
        DTOList.setPageSize(transactionMoneyPage.getSize());
        DTOList.setTotalElements(transactionMoneyPage.getTotalElements());
        DTOList.setTotalPages(transactionMoneyPage.getTotalPages());
        DTOList.setList(convertToDto(transactionMoneyPage.getContent(), TransactionMoneyDto.class));
        return DTOList;
    }

    @Override
    public List<StatisticDto> getStatisticMoney(String status, String type) {
        try {
            List<StatisticDto> statistics = new ArrayList<>();
            Map<String, LocalDateTime[]> dates = DateUtil.getListLocalDateTime(type);
            for (Map.Entry<String, LocalDateTime[]> entry : dates.entrySet()) {
                LocalDateTime fromDate = entry.getValue()[0];
                LocalDateTime toDate = entry.getValue()[1];
                Long totalMoney = getTotalMoney(status, fromDate, toDate);
                StatisticDto statisticDto = new StatisticDto();
                statisticDto.setName(entry.getKey());
                statisticDto.setValue(totalMoney == null ? 0 : totalMoney);
                statistics.add(statisticDto);
            }
            return statistics;
        } catch (Exception e) {
            throw new NotFoundException("Invalid type");
        }
    }

    @Override
    public List<StatisticDto> getStatisticUser(String type) {
        try {
            List<StatisticDto> statistics = new ArrayList<>();
            Map<String, LocalDateTime[]> dates = DateUtil.getListLocalDateTime(type);
            for (Map.Entry<String, LocalDateTime[]> entry : dates.entrySet()) {
                LocalDateTime fromDate = entry.getValue()[0];
                LocalDateTime toDate = entry.getValue()[1];
                Long totalUser = userRepository.countAllByCreatedDateBetweenAndTempAccount(fromDate, toDate, false);
                StatisticDto statisticDto = new StatisticDto();
                statisticDto.setName(entry.getKey());
                statisticDto.setValue(totalUser);
                statistics.add(statisticDto);
            }
            return statistics;
        } catch (Exception e) {
            throw new NotFoundException("Invalid type");
        }
    }

    private void sendNotificationMessage(CommonMessage message) {
        userProducer.sendMessage(message, topicUserServiceNotifyCommon);
    }
}