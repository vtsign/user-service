package tech.vtsign.userservice.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.vtsign.userservice.domain.TransactionMoney;
import tech.vtsign.userservice.domain.User;
import tech.vtsign.userservice.exception.*;
import tech.vtsign.userservice.model.UserChangePasswordDto;
import tech.vtsign.userservice.model.UserDepositDto;
import tech.vtsign.userservice.model.UserUpdateDto;
import tech.vtsign.userservice.model.zalopay.*;
import tech.vtsign.userservice.proxy.ZaloPayServiceProxy;
import tech.vtsign.userservice.repository.UserRepository;
import tech.vtsign.userservice.service.UserProducer;
import tech.vtsign.userservice.service.UserService;
import tech.vtsign.userservice.utils.TransactionConstant;
import tech.vtsign.userservice.utils.zalopay.crypto.HMACUtil;

import javax.xml.bind.DatatypeConverter;
import java.util.*;

import static tech.vtsign.userservice.utils.DateUtil.getCurrentTimeString;

@RequiredArgsConstructor
@Service
@Slf4j
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final UserProducer userProducer;
    private final ZaloPayServiceProxy zaloPayServiceProxy;

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
    @Value("${tech.vtsign.zalopay.callback-key}")
    private String callbackKey;


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
        log.error(user.toString());
        return userRepository.save(user);
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
        String key2 = callbackKey;
        String reqMac = zaloPayCallbackRequest.getMac();
        String mac = DatatypeConverter.printHexBinary(key2.getBytes()).toLowerCase();
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
            log.error("callback hợp lệ");
            ObjectMapper m = new ObjectMapper();
            DataCallBack dataCallBack = null;
            try {
                dataCallBack = m.readValue(zaloPayCallbackRequest.getData(), DataCallBack.class);
                log.info("pay success, callback data: {}", dataCallBack);
                Item item = m.readValue(dataCallBack.getItem(), Item.class);
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
                    transactionMoney.setStatus(TransactionConstant.DEPOSIT);
                    transactionMoney.setDescription("deposit money for system");
                    transactionMoney.setUser(user);
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
    public Boolean updateUserBalance(UUID userId, long amount, String method) {
        User user = this.findUserById(userId);

        TransactionMoney transactionMoney = new TransactionMoney();
        transactionMoney.setAmount(amount);
        transactionMoney.setMethod(method);
        transactionMoney.setUser(user);
        if (method.equals(TransactionConstant.DEPOSIT)) {
            transactionMoney.setStatus(TransactionConstant.DEPOSIT);
            transactionMoney.setDescription("deposit money for system");
            user.setBalance(user.getBalance() + amount);
        }
        if (method.equals(TransactionConstant.PAYMENT)) {
            if (user.getBalance() < amount) {
                return false;
            }
            transactionMoney.setStatus(TransactionConstant.PAYMENT);
            transactionMoney.setDescription("pay money for system");
            user.setBalance(user.getBalance() - amount);

        }
        return true;
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


}