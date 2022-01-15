package tech.vtsign.userservice.exception;

import org.springframework.http.HttpStatus;

public class UserBlockedException extends RuntimeException {
    public static final HttpStatus status = HttpStatus.valueOf(424);

    public UserBlockedException(String message) {
        super(message);
    }
}
