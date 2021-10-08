package tech.vtsign.userservice.exception;

import org.springframework.http.HttpStatus;

// 401
public class UnauthorizedException extends RuntimeException {
    public static final HttpStatus status = HttpStatus.UNAUTHORIZED;

    public UnauthorizedException(String message) {
        super(message);
    }
}
