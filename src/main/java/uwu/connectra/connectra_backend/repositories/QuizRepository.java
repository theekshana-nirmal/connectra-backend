package uwu.connectra.connectra_backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uwu.connectra.connectra_backend.entities.Quiz;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for Quiz entity operations.
 */
@Repository
public interface QuizRepository extends JpaRepository<Quiz, Long> {
    /**
     * Find all quizzes for a meeting, ordered by creation date descending.
     */
    List<Quiz> findAllByMeetingMeetingIdOrderByCreatedAtDesc(UUID meetingId);

    /**
     * Find the currently active quiz for a meeting.
     */
    Optional<Quiz> findByMeetingMeetingIdAndIsActiveTrue(UUID meetingId);
}
