package uwu.connectra.connectra_backend.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

@Component
public class QuizResponseStorage {
    // Key: "{quizId}_{studentId}" -> Value: Response Object
    private final Map<String, QuizResponse> responses = new ConcurrentHashMap<>();

    public void addResponse(Long quizId, Long studentId, Character answer) {
        String key = quizId + "_" + studentId;
        responses.put(key, new QuizResponse(quizId, studentId, answer, LocalDateTime.now()));
    }

    public Stream<QuizResponse> getResponsesForQuiz(Long quizId) {
        return responses.values().stream()
                .filter(r -> r.getQuizId().equals(quizId));
    }

    public void clearResponsesForQuiz(Long quizId) {
        responses.entrySet().removeIf(entry -> entry.getValue().getQuizId().equals(quizId));
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class QuizResponse {
        private Long quizId;
        private Long studentId;
        private Character selectedAnswer;
        private LocalDateTime respondedAt;
    }
}