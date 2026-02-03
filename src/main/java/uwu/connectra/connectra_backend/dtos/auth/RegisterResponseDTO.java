package uwu.connectra.connectra_backend.dtos.auth;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO for registration response (before email verification).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RegisterResponseDTO {
    private String email;
    private String message;
    private boolean requiresVerification;
}
