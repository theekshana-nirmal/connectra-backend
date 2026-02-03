package uwu.connectra.connectra_backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uwu.connectra.connectra_backend.entities.EmailVerificationToken;

import java.util.Optional;

/**
 * Repository for email verification tokens.
 */
@Repository
public interface EmailVerificationTokenRepository extends JpaRepository<EmailVerificationToken, Long> {

    /**
     * Find the most recent OTP for an email address.
     */
    Optional<EmailVerificationToken> findTopByEmailOrderByCreatedAtDesc(String email);

    /**
     * Delete all tokens for an email (cleanup after successful verification).
     */
    void deleteByEmail(String email);
}
