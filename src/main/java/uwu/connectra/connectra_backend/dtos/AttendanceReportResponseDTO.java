package uwu.connectra.connectra_backend.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class AttendanceReportResponseDTO {
    // --- 1. Meeting Metadata ---
    private String meetingId;
    private String topic;
    private String lecturerName;
    private String degree;
    private int batch;
    private LocalDateTime startedAt;
    private LocalDateTime endedAt;
    private int durationMinutes;

    // --- 2. Summary Statistics (The Numbers) ---
    private int totalStudents; // In target degree and batch
    private int totalParticipated;
    private int presentCount;
    private int partialCount;
    private int absentCount;

    // --- 3. Detailed Attendance Records ---
    // All present students (Students who attended more than or equal to 80% of the meeting duration)
    List<StudentAttendanceDTO> presentStudents;

    // All partially attended students
    List<StudentAttendanceDTO> partiallyAttendedStudents;

    // All absent students
    List<StudentAttendanceDTO> absentStudents;
}
