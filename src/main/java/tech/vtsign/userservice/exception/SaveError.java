package tech.vtsign.userservice.exception;

public class SaveError extends RuntimeException {
    public SaveError(String message) {
        super(message);
    }
}
