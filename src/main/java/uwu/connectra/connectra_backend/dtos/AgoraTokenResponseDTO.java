package uwu.connectra.connectra_backend.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class AgoraTokenResponseDTO {
    private String agoraToken;
    private String appId;
    private int uId;
    private String channelName;
}
