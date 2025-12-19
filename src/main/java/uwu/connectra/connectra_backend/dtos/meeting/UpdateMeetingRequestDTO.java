package uwu.connectra.connectra_backend.dtos.meeting;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class UpdateMeetingRequestDTO {
    @NotBlank(message = "Title is mandatory")
    @Size(max = 200, message = "Title can be at most 200 characters")
    private String title;

    @Size(max = 1000, message = "Description can be at most 1000 characters")
    private String description;

    @NotNull(message = "Scheduled start time is mandatory")
    @Future(message = "Scheduled start time must be in the future time, not past")
    private LocalDateTime scheduledStartTime;

    @NotNull(message = "Scheduled end time is mandatory")
    @Future(message = "Scheduled end time must be in the future time, not past")
    private LocalDateTime scheduledEndTime;

    @NotBlank(message = "Target degree is mandatory")
    private String targetDegree;

    @NotNull(message = "Target batch is mandatory")
    private Integer targetBatch;
}
