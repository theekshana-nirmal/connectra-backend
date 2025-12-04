package uwu.connectra.connectra_backend.repositories;

import org.springframework.stereotype.Repository;
import uwu.connectra.connectra_backend.entities.Student;

import java.util.Optional;

@Repository
public interface StudentRepository extends BaseRepository<Student, Long> {
    Optional<Student> findByEmail(String email);
}
