package uwu.connectra.connectra_backend.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uwu.connectra.connectra_backend.entities.AttendanceStatus;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class StudentAttendanceDTO {
    private String studenEnrollmentId;
    private String studentName;
    private AttendanceStatus attendanceStatus;
    private long durationMinutes; // Total duration the student was present
}
