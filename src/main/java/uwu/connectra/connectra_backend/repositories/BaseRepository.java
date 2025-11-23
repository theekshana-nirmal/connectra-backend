package uwu.connectra.connectra_backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BaseRepository<T, ID> extends JpaRepository<T, ID> {
}
