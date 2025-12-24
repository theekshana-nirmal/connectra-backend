package uwu.connectra.connectra_backend.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class StudentAttendanceHistoryResponseDTO {
    private UUID meetingId;
    private String meetingTitle;
    private LocalDateTime meetingDate;
    private String lecturerName;
    private LocalDateTime joinedAt;
    private LocalDateTime leftAt;
    private Long totalTimeInMinutes;
    private Double attendancePercentage;
    private String attendanceStatus;
    private Long meetingDuration;
}
