package tech.vtsign.userservice.exception.user;

public class UserInactiveException extends RuntimeException{
    public UserInactiveException(String message){
        super(message);
    }
}
