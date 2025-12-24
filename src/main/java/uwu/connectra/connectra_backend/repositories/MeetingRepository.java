package uwu.connectra.connectra_backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uwu.connectra.connectra_backend.entities.Meeting;
import uwu.connectra.connectra_backend.entities.MeetingStatus;

import java.util.List;
import java.util.UUID;

@Repository
public interface MeetingRepository extends JpaRepository<Meeting, UUID> {
    // Get meetings by its creator's email ordered by creation date descending
    List<Meeting> findAllByCreatedByEmailOrderByCreatedAtDesc(String email);

    // Get meetings by target degree and target batch
    List<Meeting> findAllByTargetDegreeAndTargetBatch(String targetDegree, Integer targetBatch);

    // Get meetings by target degree, target batch, and status
    List<Meeting> findAllByTargetDegreeAndTargetBatchAndStatus(String targetDegree, Integer targetBatch,
            MeetingStatus status);
}
