package uwu.connectra.connectra_backend.repositories;

import org.springframework.stereotype.Repository;
import uwu.connectra.connectra_backend.entities.Admin;

import java.util.Optional;

@Repository
public interface AdminRepository extends BaseRepository<Admin, Long> {
    Optional<Admin> findByEmail(String email);
}
