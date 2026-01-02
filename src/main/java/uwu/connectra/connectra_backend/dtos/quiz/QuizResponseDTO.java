package uwu.connectra.connectra_backend.dtos.quiz;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class QuizResponseDTO {
    private Long id;
    private String questionText;
    private String optionA;
    private String optionB;
    private String optionC;
    private String optionD;
    private Character correctAnswer;
    private Integer timeLimitSeconds;
    private boolean isActive;
    private LocalDateTime launchedAt;
    private LocalDateTime endedAt;
}