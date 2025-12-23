package uwu.connectra.connectra_backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uwu.connectra.connectra_backend.entities.Attendance;
import uwu.connectra.connectra_backend.entities.Meeting;
import uwu.connectra.connectra_backend.entities.Student;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    // Find by Student and Meeting
    Optional<Attendance> findByStudentAndMeeting(Student student, Meeting meeting);
}
