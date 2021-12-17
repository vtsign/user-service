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
import tech.vtsign.userservice.domain.Role;
import tech.vtsign.userservice.domain.TransactionMoney;
import tech.vtsign.userservice.domain.User;
import tech.vtsign.userservice.exception.*;
import tech.vtsign.userservice.model.*;
import tech.vtsign.userservice.model.zalopay.*;
import tech.vtsign.userservice.proxy.DocumentServiceProxy;
import tech.vtsign.userservice.proxy.ZaloPayServiceProxy;
import tech.vtsign.userservice.repository.RoleRepository;
import tech.vtsign.userservice.repository.TransactionMoneyRepository;
import tech.vtsign.userservice.repository.UserRepository;
import tech.vtsign.userservice.service.AzureStorageService;
import tech.vtsign.userservice.service.RoleService;
import tech.vtsign.userservice.service.UserProducer;
import tech.vtsign.userservice.service.UserService;
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
    private final RoleService roleService;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final UserProducer userProducer;
    private final ZaloPayServiceProxy zaloPayServiceProxy;
    private final DocumentServiceProxy documentServiceProxy;
    private final TransactionMoneyRepository transactionMoneyRepository;
    private final AzureStorageService azureStorageService;

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
    private final long amount = 5000;

    @Value("${tech.vtsign.zalopay.init-balance}")
    private final long initBalance = 10000;

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
        Optional<User> opt = userRepository.findById(id);
        User user = opt.orElseThrow(() -> new NotFoundException("User not found"));
        BeanUtils.copyProperties(userUpdateDto, user);

        if (userUpdateDto.getRole() != null) {
            Role roleUser = roleService.findByName(userUpdateDto.getRole());
            user.setRoles(Collections.singletonList(roleUser));
        }

        User userSave = userRepository.save(user);
        documentServiceProxy.updateUser(userSave);
        return userSave;
    }

    @Override
    public User changePassword(UUID id, UserChangePasswordDto userChangePasswordDto) {
        Optional<User> opt = userRepository.findById(id);
        User user = opt.orElseThrow(() -> new NotFoundException("User not found"));
        if (!bCryptPasswordEncoder.matches(userChangePasswordDto.getOldPassword(), user.getPassword())) {
            throw new UnauthorizedException("Invalid old password");
        }
        user.setPassword(bCryptPasswordEncoder.encode(userChangePasswordDto.getNewPassword()));
        return userRepository.save(user);
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
                    transactionMoney.setDescription("deposit money for system");
                    transactionMoney.setUser(user);
                    transactionMoneyRepository.save(transactionMoney);

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
        if (status.equals(TransactionConstant.DEPOSIT_STATUS)) {
            transactionMoney.setDescription("deposit money for system");
            user.setBalance(user.getBalance() + amount);
        }
        if (status.equals(TransactionConstant.PAYMENT_STATUS)) {
            if (user.getBalance() < amount) {
                return false;
            }
            transactionMoney.setDescription("pay money for system");
            transactionMoney.setUser(user);
            user.getTransactionMonies().add(transactionMoney);
            user.setBalance(user.getBalance() - amount);

        }
        return true;
    }

    @Override
    public Page<TransactionMoney> findAllTransactions(User user, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<TransactionMoney> transactionMoneyPage = transactionMoneyRepository.findAll(new Specification<TransactionMoney>() {
            final List<Predicate> predicates = new ArrayList<>();

            @Override
            public Predicate toPredicate(Root<TransactionMoney> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
                if (user != null) {
                    predicates.add(criteriaBuilder.and(criteriaBuilder.equal(root.get("user"), user)));
                }
                return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
            }
        }, pageable);
        return transactionMoneyPage;
    }


    @Override
    public User updateAvatar(UUID id, MultipartFile file) {
        Optional<User> opt = userRepository.findById(id);
        User user = opt.orElseThrow(() -> new NotFoundException("User not found"));
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
        log.error(activation.toString());
        log.info("url Activation", activation.getUrl());
        userProducer.sendMessage(activation);
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
        userProducer.sendMessage(activation);
        return userSave;
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
        transactionMoney.setStatus(TransactionConstant.INIT_BALANCE);
        transactionMoney.setUser(user);
        transactionMoneyRepository.save(transactionMoney);
        user.setBalance(initBalance);
        user.setEnabled(true);
        documentServiceProxy.saveUser(user);
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
        return true;
    }

    @Transactional
    @Override
    public boolean deleteUser(UUID userUUID, boolean isDelete) {
        User user = findById(userUUID);
        user.setDeleted(isDelete);
        return true;
    }

    @Override
    public long countUserBetweenDate(LocalDateTime from, LocalDateTime to) {
        return userRepository.countAllByCreatedDateBetween(from, to);
    }


    @Override
    @Transactional
    public boolean updateRoleUser(UUID userId, String nameRole) {
        Optional<User> userOpt = userRepository.findById(userId);
        User user = userOpt.orElseThrow(() -> new NotFoundException("User not found!"));
        Optional<Role> roleOpt = roleRepository.findByName(nameRole);
        Role role = roleOpt.orElseThrow(() -> new NotFoundException("Role not found"));
        user.setRoles(List.of(role));
        return true;
    }

    @Override
    public UserManagementList getUserManagementList(int page, int pageSize, String sortField, String sortType, String keyword) {
        Sort sort = Sort.by(sortField).ascending();
        if (sortType.equals("desc")) {
            sort = Sort.by(sortField).descending();
        }
        Pageable pageable = PageRequest.of(page - 1, pageSize, sort);

        Page<User> userPage;
        if (keyword != null && !keyword.isEmpty()) {
            userPage = userRepository.findAll(keyword, pageable);
        } else {
            userPage = userRepository.findAll(pageable);
        }

        UserManagementList userManagementList = new UserManagementList();
        userManagementList.setPage(page);
        userManagementList.setPageSize(userPage.getSize());
        userManagementList.setTotalElements(userPage.getTotalElements());
        userManagementList.setTotalPages(userPage.getTotalPages());
        userManagementList.setUsers(convertToUserResponseDtoList(userPage.getContent()));
        return userManagementList;
    }

    private List<UserResponseDto> convertToUserResponseDtoList(List<User> users) {
        List<UserResponseDto> userResponseDtoList = new ArrayList<>();
        for (User user : users) {
            UserResponseDto userResponseDto = new UserResponseDto();
            BeanUtils.copyProperties(user, userResponseDto);
            userResponseDtoList.add(userResponseDto);
        }
        return userResponseDtoList;
    }

    @Override
    public Long getTotalMoney(String status, LocalDateTime fromDate, LocalDateTime toDate) {
        return transactionMoneyRepository.getSumAmountByStatus(status, fromDate, toDate);
    }

}