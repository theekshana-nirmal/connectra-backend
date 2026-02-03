package uwu.connectra.connectra_backend.exceptions;

/**
 * Exception thrown when an OTP is invalid.
 */
public class OtpInvalidException extends RuntimeException {
    public OtpInvalidException(String message) {
        super(message);
    }
}
