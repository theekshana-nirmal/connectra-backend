package uwu.connectra.connectra_backend.dtos.quiz;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for active quiz (used by students).
 * Does NOT include correctAnswer - students shouldn't see it.
 * Includes calculated timeRemainingSeconds.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActiveQuizResponseDTO {
    private Long id;
    private String question;
    private String optionA;
    private String optionB;
    private String optionC;
    private String optionD;
    private Integer timeRemainingSeconds;
}
