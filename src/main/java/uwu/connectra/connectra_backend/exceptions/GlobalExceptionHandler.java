package uwu.connectra.connectra_backend.exceptions;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import uwu.connectra.connectra_backend.dtos.ErrorResponseDTO;

public class GlobalExceptionHandler {
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> handleAllExceptions(Exception ex) {
        ErrorResponseDTO errorResponse = new ErrorResponseDTO(
                500,
                ex.getMessage(),
                System.currentTimeMillis()
        );
        return ResponseEntity.status(500).body(errorResponse);
    }

    // Handle User Already Exists Exception
    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ErrorResponseDTO> handleUserAlreadyExistsException(UserAlreadyExistsException ex) {
        ErrorResponseDTO errorResponse = new ErrorResponseDTO(
                400,
                ex.getMessage(),
                System.currentTimeMillis()
        );
        return ResponseEntity.status(400).body(errorResponse);
    }

    // Handle User Credentials Invalid Exception
    @ExceptionHandler(UserCredentialsInvalidException.class)
    public ResponseEntity<ErrorResponseDTO> handleUserCredentialsInvalidException(UserCredentialsInvalidException ex) {
        ErrorResponseDTO errorResponse = new ErrorResponseDTO(
                401,
                ex.getMessage(),
                System.currentTimeMillis()
        );
        return ResponseEntity.status(401).body(errorResponse);
    }

    // Handle User Not Found Exception
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleUserNotFoundException(UserNotFoundException ex) {
        ErrorResponseDTO errorResponse = new ErrorResponseDTO(
                404,
                ex.getMessage(),
                System.currentTimeMillis()
        );
        return ResponseEntity.status(404).body(errorResponse);
    }

    // Invalid Role Exception
    @ExceptionHandler(InvalidRoleException.class)
    public ResponseEntity<ErrorResponseDTO> handleInvalidRoleException(InvalidRoleException ex) {
        ErrorResponseDTO errorResponse = new ErrorResponseDTO(
                400,
                ex.getMessage(),
                System.currentTimeMillis()
        );
        return ResponseEntity.status(403).body(errorResponse);
    }

    // Invalid Token Exception
    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<ErrorResponseDTO> handleInvalidTokenException(InvalidTokenException ex) {
        ErrorResponseDTO errorResponse = new ErrorResponseDTO(
                401,
                ex.getMessage(),
                System.currentTimeMillis()
        );
        return ResponseEntity.status(401).body(errorResponse);
    }
}