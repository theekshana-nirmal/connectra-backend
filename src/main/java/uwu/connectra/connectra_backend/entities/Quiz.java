package uwu.connectra.connectra_backend.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Quiz {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meeting_id", nullable = false)
    private Meeting meeting;

    @NotBlank
    @Column(length = 500)
    private String questionText;

    @NotBlank
    private String optionA;

    @NotBlank
    private String optionB;

    private String optionC;
    private String optionD;

    @NotNull
    private Character correctAnswer; // 'A', 'B', 'C', or 'D'

    @NotNull
    private Integer timeLimitSeconds; // 30, 60, 120

    private boolean isActive = false;

    private LocalDateTime launchedAt;
    private LocalDateTime endedAt;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}