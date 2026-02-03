package uwu.connectra.connectra_backend.dtos.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO for resending OTP request.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ResendOtpRequestDTO {

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Pattern(regexp = ".*@std\\.uwu\\.ac\\.lk$", message = "Must be a valid student email (@std.uwu.ac.lk)")
    private String email;
}
