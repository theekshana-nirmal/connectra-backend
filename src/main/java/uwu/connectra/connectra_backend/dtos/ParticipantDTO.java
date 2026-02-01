package uwu.connectra.connectra_backend.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO for representing a participant in a meeting.
 * Used to sync participant names with Agora UIDs.
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ParticipantDTO {
    private Integer agoraUid;
    private String displayName;
    private boolean isHost;
}
