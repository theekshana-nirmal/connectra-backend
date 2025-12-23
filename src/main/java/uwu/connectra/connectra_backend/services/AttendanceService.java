package uwu.connectra.connectra_backend.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uwu.connectra.connectra_backend.entities.Attendance;
import uwu.connectra.connectra_backend.entities.Meeting;
import uwu.connectra.connectra_backend.entities.Student;
import uwu.connectra.connectra_backend.exceptions.UnauthorizedException;
import uwu.connectra.connectra_backend.repositories.AttendanceRepository;
import uwu.connectra.connectra_backend.utils.CurrentUserProvider;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AttendanceService {
    private final CurrentUserProvider currentUserProvider;
    private final AttendanceRepository attendanceRepository;

    // Helpers to record attendance
    // Update student attendance on join
    public void recordStudentAttendanceOnJoin(Meeting meeting) {
        Student currentStudent = currentUserProvider.getCurrentUserAs(Student.class);
        LocalDateTime now = LocalDateTime.now();

        // Create or update attendance record
        Attendance attendance = attendanceRepository.findByStudentAndMeeting(currentStudent, meeting)
                .orElseGet(() -> {
                    Attendance newAttendance = new Attendance();
                    newAttendance.setStudent(currentStudent);
                    newAttendance.setMeeting(meeting);
                    newAttendance.setJoinedAt(now);
                    return newAttendance;
                });

        attendance.setLastJoinedAt(now);
        attendanceRepository.save(attendance);
    }

    // Update student attendance on leave and calculate total duration
    public void recordStudentAttendanceOnLeave(Meeting meeting) {
        Student currentStudent = currentUserProvider.getCurrentUserAs(Student.class);

        // Update attendance
        Attendance attendance = attendanceRepository.findByStudentAndMeeting(currentStudent, meeting).orElseThrow(
                () -> new UnauthorizedException("Attendance record not found for student in this meeting.")
        );

        LocalDateTime now = LocalDateTime.now();
        attendance.setLeftAt(now);

        long duration = java.time.Duration.between(attendance.getLastJoinedAt(), now).toMinutes();
        attendance.setTotalDurationInMinutes(attendance.getTotalDurationInMinutes() + duration);

        attendanceRepository.save(attendance);
    }
}
