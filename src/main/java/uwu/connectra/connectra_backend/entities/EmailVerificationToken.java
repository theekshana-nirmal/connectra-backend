package uwu.connectra.connectra_backend.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Entity to store email verification OTP tokens and pending registration data.
 * Each OTP is valid for 10 minutes and can only be used once.
 * User is NOT created in DB until OTP is verified.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "email_verification_tokens")
@EntityListeners(AuditingEntityListener.class)
public class EmailVerificationToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false, length = 6)
    private String otp;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @Column(nullable = false)
    private boolean verified = false;

    // Pending registration data - stored until OTP is verified
    @Column
    private String firstName;

    @Column
    private String lastName;

    @Column
    private String hashedPassword;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    public EmailVerificationToken(String email, String otp, LocalDateTime expiresAt,
            String firstName, String lastName, String hashedPassword) {
        this.email = email;
        this.otp = otp;
        this.expiresAt = expiresAt;
        this.firstName = firstName;
        this.lastName = lastName;
        this.hashedPassword = hashedPassword;
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }
}
