package uwu.connectra.connectra_backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uwu.connectra.connectra_backend.entities.Attendance;
import uwu.connectra.connectra_backend.entities.AttendanceStatus;
import uwu.connectra.connectra_backend.entities.Meeting;
import uwu.connectra.connectra_backend.entities.Student;

import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    // Find by Student and Meeting
    Optional<Attendance> findByStudentAndMeeting(Student student, Meeting meeting);

    // Get all Attendance records for a Meeting
    List<Attendance> findAllByMeeting(Meeting meeting);

    // Get students by attendance status for a specific meeting
    List<Attendance> findAllByMeetingAndAttendanceStatus(Meeting meeting, AttendanceStatus attendanceStatus);
}
