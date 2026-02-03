package uwu.connectra.connectra_backend.exceptions;

/**
 * Exception thrown when an OTP has expired.
 */
public class OtpExpiredException extends RuntimeException {
    public OtpExpiredException(String message) {
        super(message);
    }
}
