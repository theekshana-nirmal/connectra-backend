package uwu.connectra.connectra_backend.exceptions;

public class MeetingAlreadyEndedException extends RuntimeException {
    public MeetingAlreadyEndedException(String message) {
        super(message);
    }
}
