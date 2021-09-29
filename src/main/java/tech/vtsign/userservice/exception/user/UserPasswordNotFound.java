package tech.vtsign.userservice.exception.user;

public class UserPasswordNotFound extends RuntimeException {
    public UserPasswordNotFound(String message) {
        super(message);
    }
}
