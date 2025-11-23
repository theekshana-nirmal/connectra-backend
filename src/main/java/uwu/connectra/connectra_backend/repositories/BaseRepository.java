package uwu.connectra.connectra_backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean // Prevents Spring from trying to create an instance
public interface BaseRepository<T, ID> extends JpaRepository<T, ID> {
    boolean existsByEmail(String email);
}