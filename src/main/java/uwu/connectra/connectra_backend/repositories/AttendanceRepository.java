package uwu.connectra.connectra_backend.repositories;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    // Find by Student and Meeting with pessimistic lock to prevent race conditions
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM Attendance a WHERE a.student = :student AND a.meeting = :meeting")
    Optional<Attendance> findByStudentAndMeetingWithLock(
            @Param("student") Student student,
            @Param("meeting") Meeting meeting);

    // Get all Attendance records for a Meeting
    List<Attendance> findAllByMeeting(Meeting meeting);

    // Get all Attendance records for a Meeting with student eagerly fetched
    @Query("SELECT a FROM Attendance a JOIN FETCH a.student WHERE a.meeting = :meeting")
    List<Attendance> findAllByMeetingWithStudent(@Param("meeting") Meeting meeting);

    // Get students by attendance status for a specific meeting
    List<Attendance> findAllByMeetingAndAttendanceStatus(Meeting meeting, AttendanceStatus attendanceStatus);
}
