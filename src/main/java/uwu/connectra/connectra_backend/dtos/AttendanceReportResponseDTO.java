package uwu.connectra.connectra_backend.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class AttendanceReportResponseDTO {
    private String meetingId;
    private String meetingTopic;
    private int totalParticipants;
    private int presentParticipants;
    private int absentParticipants;
}
