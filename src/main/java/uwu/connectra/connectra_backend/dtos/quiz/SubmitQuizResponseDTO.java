package uwu.connectra.connectra_backend.dtos.quiz;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SubmitQuizResponseDTO {
    @NotNull(message = "Selected answer is required")
    private Character selectedAnswer;
}