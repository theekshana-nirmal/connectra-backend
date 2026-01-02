package uwu.connectra.connectra_backend.dtos.quiz;

import lombok.Data;
import java.util.Map;

@Data
public class QuizResultsSummaryDTO {
    private Long quizId;
    private String questionText;
    private Character correctAnswer;
    private long totalStudentsInMeeting;
    private long totalResponses;
    private long correctCount;
    private long incorrectCount;
    private double responseRate;
    private Map<Character, Long> responseBreakdown;
}