package uwu.connectra.connectra_backend.exceptions;

public class UserCredentialsInvalidException extends RuntimeException {
    public UserCredentialsInvalidException(String message) {
        super(message);
    }
}
