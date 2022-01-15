package tech.vtsign.userservice.exception;

import org.springframework.http.HttpStatus;

public class UserDeletedException extends RuntimeException {
    public static final HttpStatus status = HttpStatus.valueOf(425);

    public UserDeletedException(String message) {
        super(message);
    }
}
