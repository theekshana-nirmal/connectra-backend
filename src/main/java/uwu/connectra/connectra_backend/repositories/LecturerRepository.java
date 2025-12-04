package uwu.connectra.connectra_backend.repositories;

import org.springframework.stereotype.Repository;
import uwu.connectra.connectra_backend.entities.Lecturer;

import java.util.Optional;

@Repository
public interface LecturerRepository extends BaseRepository<Lecturer, Long> {
    Optional<Lecturer> findByEmail(String email);
}
