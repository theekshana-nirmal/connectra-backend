package uwu.connectra.connectra_backend.dtos.meeting;

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
public class MeetingResponseDTO {
    private UUID meetingId;
    private String title;
    private String description;
    private LocalDateTime scheduledStartTime;
    private LocalDateTime scheduledEndTime;
    private LocalDateTime actualStartTime;
    private LocalDateTime actualEndTime;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String targetDegree;
    private Integer targetBatch;
    private String status;

    private Long createdById;
    private String createdByName;
}