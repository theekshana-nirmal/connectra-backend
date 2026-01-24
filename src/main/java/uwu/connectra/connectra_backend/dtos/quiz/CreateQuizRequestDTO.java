package uwu.connectra.connectra_backend.dtos.quiz;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uwu.connectra.connectra_backend.entities.CorrectAnswer;

/**
 * Request DTO for creating a new quiz.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateQuizRequestDTO {
    @NotBlank(message = "Question text is required")
    @Size(max = 1000, message = "Question text must be at most 1000 characters")
    private String questionText;

    @NotBlank(message = "Option A is required")
    @Size(max = 500, message = "Option A must be at most 500 characters")
    private String optionA;

    @NotBlank(message = "Option B is required")
    @Size(max = 500, message = "Option B must be at most 500 characters")
    private String optionB;

    @NotBlank(message = "Option C is required")
    @Size(max = 500, message = "Option C must be at most 500 characters")
    private String optionC;

    @NotBlank(message = "Option D is required")
    @Size(max = 500, message = "Option D must be at most 500 characters")
    private String optionD;

    @NotNull(message = "Correct answer is required")
    private CorrectAnswer correctAnswer;

    @NotNull(message = "Time limit is required")
    private Integer timeLimitSeconds;
}
