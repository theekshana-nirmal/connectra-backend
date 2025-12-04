package uwu.connectra.connectra_backend.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class StudentAuthResponseDTO {
    private String firstName;
    private String lastName;
    private String email;
    private String role;
    private String profilePhotoUrl;
    private String degree;
    private int batch;
    private String accessToken;
    private long expiresIn;
}
