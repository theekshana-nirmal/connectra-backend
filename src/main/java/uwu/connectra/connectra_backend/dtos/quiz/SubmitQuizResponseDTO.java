package uwu.connectra.connectra_backend.dtos.quiz;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uwu.connectra.connectra_backend.entities.CorrectAnswer;

/**
 * Request DTO for submitting a quiz response.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubmitQuizResponseDTO {
    @NotNull(message = "Selected answer is required")
    private CorrectAnswer selectedAnswer;
}
