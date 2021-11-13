package tech.vtsign.userservice.controller;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import tech.vtsign.userservice.domain.User;
import tech.vtsign.userservice.exception.ExceptionResponse;
import tech.vtsign.userservice.exception.MissingFieldException;
import tech.vtsign.userservice.model.UserLoginDto;
import tech.vtsign.userservice.model.UserRequestDto;
import tech.vtsign.userservice.model.UserResponseDto;
import tech.vtsign.userservice.model.zalopay.*;
import tech.vtsign.userservice.proxy.ZaloPayServiceProxy;
import tech.vtsign.userservice.service.UserService;
import tech.vtsign.userservice.utils.zalopay.crypto.HMACUtil;

import javax.xml.bind.DatatypeConverter;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@RestController
@Tag(name = "Accept controller")
@Slf4j
@RequestMapping("/apt")
public class AcceptController {

    private final UserService userService;
    private final ZaloPayServiceProxy zaloPayServiceProxy;

    @Hidden
    @Operation(summary = "Get user by email [service call only]")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found the user",
                    content = {
                            @Content(mediaType = "application/json", schema = @Schema(implementation = UserResponseDto.class))
                    }),
            @ApiResponse(responseCode = "422", description = "Invalid email format",
                    content = {
                            @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))
                    }),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = {
                            @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))
                    })
    })
    @GetMapping("/")
    public ResponseEntity<UserResponseDto> retrieveUser(@RequestParam("email") String email) {
        User user = userService.findByEmail(email);
        UserResponseDto userRes = new UserResponseDto();
        BeanUtils.copyProperties(user, userRes);
        return ResponseEntity.ok().body(userRes);
    }

    @Hidden
    @GetMapping("/email")
    public ResponseEntity<UserResponseDto> retrieveUserByEmail(@RequestParam String email,
                                                               @RequestParam(required = false) String phone,
                                                               @RequestParam(required = false) String name)
            throws NoSuchAlgorithmException {
        User user = userService.getOrCreateUser(email, phone, name);
        UserResponseDto userRes = new UserResponseDto();
        BeanUtils.copyProperties(user, userRes);
        return ResponseEntity.ok().body(userRes);
    }

    @Hidden
    @GetMapping("/uuid")
    public ResponseEntity<UserResponseDto> retrieveUser(@RequestParam("user_uuid") UUID userUUID) {
        System.out.println("User id: " + userUUID);
        User user = userService.findUserById(userUUID);
        UserResponseDto userRes = new UserResponseDto();
        BeanUtils.copyProperties(user, userRes);
        return ResponseEntity.ok().body(userRes);
    }


    @Hidden
    @Operation(summary = "Register account [service call only]")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Success, user registered",
                    content = {
                            @Content(mediaType = "application/json", schema = @Schema(implementation = UserResponseDto.class))
                    }),
            @ApiResponse(responseCode = "419", description = "Missing require field see message for more details",
                    content = {
                            @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))
                    }),
            @ApiResponse(responseCode = "409", description = "Email is already in use",
                    content = {
                            @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))
                    })
    })
    @PostMapping("/register")
    public ResponseEntity<UserResponseDto> register(@Validated @RequestBody UserRequestDto userRequestDto, BindingResult result) {
        if (result.hasErrors()) {
            String errorMessage = result.getAllErrors()
                    .stream().map(DefaultMessageSourceResolvable::getDefaultMessage)
                    .collect(Collectors.joining(";"));

            throw new MissingFieldException(errorMessage);
        }
        User user = new User();
        BeanUtils.copyProperties(userRequestDto, user);
        userService.save(user);
        UserResponseDto responseDto = new UserResponseDto();
        BeanUtils.copyProperties(user, responseDto);

        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);

    }

    @Hidden
    @Operation(summary = "Login account [service call only]")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login successfully",
                    content = {
                            @Content(mediaType = "application/json", schema = @Schema(implementation = UserResponseDto.class))
                    }),
            @ApiResponse(responseCode = "423", description = "User inactive",
                    content = {
                            @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))
                    }),
            @ApiResponse(responseCode = "403", description = "Invalid email password",
                    content = {
                            @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))
                    }),
            @ApiResponse(responseCode = "419", description = "Invalid email format",
                    content = {
                            @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))
                    }),
            @ApiResponse(responseCode = "419", description = "Email or password missing",
                    content = {
                            @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))
                    }),
    })
    @PostMapping("/login")
    public ResponseEntity<UserResponseDto> login(@Validated @RequestBody UserLoginDto userLoginDto, BindingResult result) {
        if (result.hasErrors()) {
            String errorMessage = result.getAllErrors()
                    .stream().map(DefaultMessageSourceResolvable::getDefaultMessage)
                    .collect(Collectors.joining(";"));

            throw new MissingFieldException(errorMessage);
        }
        Optional<User> opt = userService.login(userLoginDto.getEmail(), userLoginDto.getPassword());
        UserResponseDto userResponseDto = new UserResponseDto();
        BeanUtils.copyProperties(opt.get(), userResponseDto);
        return ResponseEntity.status(HttpStatus.OK).body(userResponseDto);
    }


    @SneakyThrows
    @Operation(summary = "Account activation")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success, Account has been activated",
                    content = {
                            @Content(mediaType = "application/json", schema = @Schema(implementation = Boolean.class))
                    }),
            @ApiResponse(responseCode = "400", description = "Link active not exist",
                    content = {
                            @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))
                    }),
            @ApiResponse(responseCode = "410", description = "Link expired",
                    content = {
                            @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))
                    })
    })

    @GetMapping("/activation/{id}")
    public ResponseEntity<Boolean> activation(@PathVariable UUID id) throws NoSuchAlgorithmException {
        boolean active = userService.activation(id);
        return ResponseEntity.ok(active);
    }

    // CC, ATM, zalopayapp
    @GetMapping("/order")
    public ResponseEntity<ZaloPayResponse> createOrder(
            @RequestParam(required = false, defaultValue = "zalopayapp") String type) throws JsonProcessingException {
        List<Item> items = List.of(
                new Item("item123", "item123", 5, 100000)
        );
        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        String key1 = "PcY4iZIKFCIdgZvA6ueMcMHHUbRLYjPL";

        OAOrder oaOrder = new OAOrder();
        oaOrder.setAppId(2553);
        oaOrder.setAppTransId(getCurrentTimeString("yyMMdd") +"_"+ new Random().nextInt(100000));
        oaOrder.setAppTime(new Date().getTime());
        oaOrder.setAppUser("user123");
        oaOrder.setAmount(items.stream().mapToInt(Item::getAmount).sum());
        oaOrder.setDescription("VTSign order id " + UUID.randomUUID());
        if(type.equals("ATM")){
            oaOrder.setBankCode("");
            oaOrder.setEmbedData("{\"bankgroup\":\"ATM\"}");
        } else {
            oaOrder.setBankCode(type);
            oaOrder.setEmbedData("{}");
        }
        oaOrder.setRedirectUrl("https://vtsign.tech/user/profile");
        oaOrder.setCallbackUrl("https://api.vtsign.tech/user/apt/order/callback");
        oaOrder.setItem(ow.writeValueAsString(items));


        // app_id +”|”+ app_trans_id +”|”+ appuser +”|”+ amount +"|" + app_time +”|”+ embed_data +"|" +item
        String data = oaOrder.getAppId() +"|"+ oaOrder.getAppTransId() +"|"+ oaOrder.getAppUser() +"|"+ oaOrder.getAmount()
                +"|"+ oaOrder.getAppTime() +"|"+ oaOrder.getEmbedData() +"|"+ oaOrder.getItem();
        oaOrder.setMac(HMACUtil.HMacHexStringEncode(HMACUtil.HMACSHA256, key1, data));

        ZaloPayResponse zaloPayResponse = zaloPayServiceProxy.createOrder(oaOrder);
        return ResponseEntity.ok(zaloPayResponse);
    }

    @PostMapping("/order/callback")
    public ResponseEntity<String> orderCallback(
            @RequestBody ZaloPayCallbackRequest zaloPayCallbackRequest) throws JsonProcessingException {
        log.info("orderCallback: {}", zaloPayCallbackRequest);
        String key2 = "PcY4iZIKFCIdgZvA6ueMcMHHUbRLYjPL";
        String reqMac = zaloPayCallbackRequest.getMac();
        String mac = DatatypeConverter.printHexBinary(key2.getBytes()).toLowerCase();
        ZaloPayCallBackResponse response = new ZaloPayCallBackResponse();
        // kiểm tra callback hợp lệ (đến từ ZaloPay server)
        if (!reqMac.equals(mac)) {
            // callback không hợp lệ
            response.setCode(-1);
            response.setMessage("mac not equal");
        } else {
            // thanh toán thành công
            // merchant cập nhật trạng thái cho đơn hàng
            ObjectMapper m = new ObjectMapper();
            DataCallBack dataCallBack = null;
            try {
                dataCallBack = m.readValue(zaloPayCallbackRequest.getData(), DataCallBack.class);
                log.info("pay success, callback data: {}", dataCallBack);
                response.setCode(1);
                response.setMessage("success");
            } catch (JsonProcessingException ex) {
                response.setCode(0);
                response.setMessage(ex.getMessage());
                ex.printStackTrace();
            }
        }
        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        String json = ow.writeValueAsString(response);
        return ResponseEntity.ok(json);
    }
    private static String getCurrentTimeString(String format) {
        Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("GMT+7"));
        SimpleDateFormat fmt = new SimpleDateFormat(format);
        fmt.setCalendar(cal);
        return fmt.format(cal.getTimeInMillis());
    }
}
