package uwu.connectra.connectra_backend.dtos.quiz;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uwu.connectra.connectra_backend.entities.CorrectAnswer;

import java.time.LocalDateTime;

/**
 * Response DTO for quiz data (used by lecturers).
 * Includes correctAnswer since lecturers should see it.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuizResponseDTO {
    private Long id;
    private String question;
    private String optionA;
    private String optionB;
    private String optionC;
    private String optionD;
    private CorrectAnswer correctAnswer;
    private Integer timeLimitSeconds;
    private Boolean isActive;
    private LocalDateTime createdAt;
}
