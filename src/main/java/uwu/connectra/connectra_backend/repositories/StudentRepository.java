package uwu.connectra.connectra_backend.repositories;

import org.springframework.stereotype.Repository;
import uwu.connectra.connectra_backend.entities.Student;

@Repository
public interface StudentRepository extends BaseRepository<Student, Long> {
}
