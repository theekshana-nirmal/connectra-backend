package uwu.connectra.connectra_backend.dtos.lecturer;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class LecturerResponseDTO {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
}
