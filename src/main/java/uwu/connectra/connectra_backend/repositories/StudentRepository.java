package uwu.connectra.connectra_backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uwu.connectra.connectra_backend.entities.Student;

import java.util.List;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {
    // Find all students by their degree and batch
    List<Student> findAllByDegreeAndBatch(String degree, int batch);
}
