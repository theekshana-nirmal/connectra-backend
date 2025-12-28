package uwu.connectra.connectra_backend.dtos.quiz;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class CreateQuizRequestDTO {
    @NotBlank(message = "Question text is required")
    @Size(max = 500)
    private String questionText;

    @NotBlank(message = "Option A is required")
    private String optionA;

    @NotBlank(message = "Option B is required")
    private String optionB;

    private String optionC;
    private String optionD;

    @NotNull(message = "Correct answer is required")
    private Character correctAnswer;

    @NotNull
    private Integer timeLimitSeconds; // Validate 30, 60, 120 in service or custom validator
}