package uwu.connectra.connectra_backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uwu.connectra.connectra_backend.entities.Meeting;
import uwu.connectra.connectra_backend.entities.Quiz;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuizRepository extends JpaRepository<Quiz, Long> {
    List<Quiz> findByMeetingOrderByLaunchedAtDesc(Meeting meeting);
    Optional<Quiz> findByMeetingAndIsActiveTrue(Meeting meeting);
}