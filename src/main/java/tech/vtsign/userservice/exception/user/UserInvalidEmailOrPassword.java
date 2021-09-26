package tech.vtsign.userservice.exception.user;

public class UserInvalidEmailOrPassword extends RuntimeException {
    public UserInvalidEmailOrPassword(String message) {
        super(message);
    }
}
