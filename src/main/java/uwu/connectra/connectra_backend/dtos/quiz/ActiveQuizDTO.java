package uwu.connectra.connectra_backend.dtos.quiz;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ActiveQuizDTO {
    private Long id;
    private String questionText;
    private String optionA;
    private String optionB;
    private String optionC;
    private String optionD;
    private Integer timeLimitSeconds;
    private LocalDateTime launchedAt;
    private long timeRemainingSeconds;
}