package uwu.connectra.connectra_backend.dtos.quiz;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uwu.connectra.connectra_backend.entities.CorrectAnswer;

/**
 * Response DTO for quiz results summary.
 * Shows counts of responses per option.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuizResultsSummaryDTO {
    private Integer totalResponses;
    private Integer correctResponses;
    private Integer optionACount;
    private Integer optionBCount;
    private Integer optionCCount;
    private Integer optionDCount;
    private CorrectAnswer correctAnswer;
}
