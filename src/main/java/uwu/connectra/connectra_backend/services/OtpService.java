package uwu.connectra.connectra_backend.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uwu.connectra.connectra_backend.entities.EmailVerificationToken;
import uwu.connectra.connectra_backend.exceptions.OtpExpiredException;
import uwu.connectra.connectra_backend.exceptions.OtpInvalidException;
import uwu.connectra.connectra_backend.repositories.EmailVerificationTokenRepository;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Service for OTP generation and validation.
 * Handles creation, storage, and verification of one-time passwords.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OtpService {

    private final EmailVerificationTokenRepository tokenRepository;

    // OTP expiration time in minutes
    private static final int OTP_EXPIRATION_MINUTES = 10;

    // SecureRandom for cryptographically secure OTP generation
    private final SecureRandom secureRandom = new SecureRandom();

    /**
     * Generates a 6-digit OTP code.
     */
    public String generateOtp() {
        int otp = 100000 + secureRandom.nextInt(900000);
        return String.valueOf(otp);
    }

    /**
     * Creates and stores a new verification token for the given email.
     * Returns the generated OTP.
     */
    @Transactional
    public String createVerificationToken(String email) {
        String otp = generateOtp();
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(OTP_EXPIRATION_MINUTES);

        EmailVerificationToken token = new EmailVerificationToken(email, otp, expiresAt);
        tokenRepository.save(token);

        log.info("Created verification token for email: {}", email);
        return otp;
    }

    /**
     * Verifies the OTP for the given email.
     * Throws OtpExpiredException if the OTP has expired.
     * Throws OtpInvalidException if the OTP is incorrect.
     */
    @Transactional
    public void verifyOtp(String email, String otp) {
        Optional<EmailVerificationToken> tokenOpt = tokenRepository.findTopByEmailOrderByCreatedAtDesc(email);

        if (tokenOpt.isEmpty()) {
            log.warn("No OTP found for email: {}", email);
            throw new OtpInvalidException("No verification code found. Please request a new one.");
        }

        EmailVerificationToken token = tokenOpt.get();

        // Check if already verified
        if (token.isVerified()) {
            log.warn("OTP already used for email: {}", email);
            throw new OtpInvalidException("This verification code has already been used.");
        }

        // Check if expired
        if (token.isExpired()) {
            log.warn("OTP expired for email: {}", email);
            throw new OtpExpiredException("Verification code has expired. Please request a new one.");
        }

        // Check if OTP matches
        if (!token.getOtp().equals(otp)) {
            log.warn("Invalid OTP provided for email: {}", email);
            throw new OtpInvalidException("Invalid verification code. Please try again.");
        }

        // Mark as verified
        token.setVerified(true);
        tokenRepository.save(token);

        log.info("OTP verified successfully for email: {}", email);
    }

    /**
     * Deletes all tokens for an email after successful account activation.
     */
    @Transactional
    public void cleanupTokens(String email) {
        tokenRepository.deleteByEmail(email);
        log.info("Cleaned up verification tokens for email: {}", email);
    }
}
