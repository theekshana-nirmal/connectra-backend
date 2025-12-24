package uwu.connectra.connectra_backend.dtos.auth;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class UserAuthResponseDTO {
    private String email;
    private String role;
    private String accessToken;
    private long expiresIn;
}
