package uwu.connectra.connectra_backend.exceptions;

public class MeetingNotFoundException extends RuntimeException {
    public MeetingNotFoundException(String message) {
        super(message);
    }
}
