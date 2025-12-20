package uwu.connectra.connectra_backend.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class AgoraTokenRequestDTO {
    @NotBlank(message = "Channel name must not be blank")
    private String channelName;
}
