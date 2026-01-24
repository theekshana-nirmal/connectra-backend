package uwu.connectra.connectra_backend.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uwu.connectra.connectra_backend.dtos.quiz.*;
import uwu.connectra.connectra_backend.entities.CorrectAnswer;
import uwu.connectra.connectra_backend.entities.Meeting;
import uwu.connectra.connectra_backend.entities.Quiz;
import uwu.connectra.connectra_backend.exceptions.ResourceNotFoundException;
import uwu.connectra.connectra_backend.exceptions.BadRequestException;
import uwu.connectra.connectra_backend.repositories.MeetingRepository;
import uwu.connectra.connectra_backend.repositories.QuizRepository;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Service for managing quiz operations.
 * Quiz responses are stored in-memory (not persisted to database).
 */
@Service
@RequiredArgsConstructor
public class QuizService {
    private final QuizRepository quizRepository;
    private final MeetingRepository meetingRepository;

    /**
     * In-memory storage for quiz responses.
     * Key: quizId, Value: Map of (studentId -> QuizResponseEntry)
     */
    private final ConcurrentHashMap<Long, ConcurrentHashMap<Long, QuizResponseEntry>> quizResponses = new ConcurrentHashMap<>();

    /**
     * Internal class to store quiz response data in memory.
     */
    private record QuizResponseEntry(Long studentId, CorrectAnswer selectedAnswer, LocalDateTime answeredAt,
            boolean isCorrect) {
    }

    // ============ LECTURER OPERATIONS ============

    /**
     * Create a new quiz for a meeting.
     */
    @Transactional
    public QuizResponseDTO createQuiz(String meetingId, CreateQuizRequestDTO request) {
        UUID uuid = parseUUID(meetingId);
        Meeting meeting = meetingRepository.findById(uuid)
                .orElseThrow(() -> new ResourceNotFoundException("Meeting not found with ID: " + meetingId));

        Quiz quiz = new Quiz();
        quiz.setMeeting(meeting);
        quiz.setQuestion(request.getQuestionText());
        quiz.setOptionA(request.getOptionA());
        quiz.setOptionB(request.getOptionB());
        quiz.setOptionC(request.getOptionC());
        quiz.setOptionD(request.getOptionD());
        quiz.setCorrectAnswer(request.getCorrectAnswer());
        quiz.setTimeLimitSeconds(request.getTimeLimitSeconds());
        quiz.setIsActive(false);

        Quiz savedQuiz = quizRepository.save(quiz);
        return mapToQuizResponseDTO(savedQuiz);
    }

    /**
     * Get all quizzes for a meeting.
     */
    @Transactional(readOnly = true)
    public List<QuizResponseDTO> getQuizzesByMeetingId(String meetingId) {
        UUID uuid = parseUUID(meetingId);
        List<Quiz> quizzes = quizRepository.findAllByMeetingMeetingIdOrderByCreatedAtDesc(uuid);
        return quizzes.stream()
                .map(this::mapToQuizResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Launch a quiz (set it as active).
     */
    @Transactional
    public void launchQuiz(Long quizId) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz not found with ID: " + quizId));

        if (quiz.getIsActive()) {
            throw new BadRequestException("Quiz is already active");
        }

        // Deactivate any other active quiz for this meeting
        UUID meetingId = quiz.getMeeting().getMeetingId();
        quizRepository.findByMeetingMeetingIdAndIsActiveTrue(meetingId)
                .ifPresent(activeQuiz -> {
                    activeQuiz.setIsActive(false);
                    activeQuiz.setEndedAt(LocalDateTime.now());
                    quizRepository.save(activeQuiz);
                });

        // Initialize in-memory response storage for this quiz
        quizResponses.put(quizId, new ConcurrentHashMap<>());

        quiz.setIsActive(true);
        quiz.setLaunchedAt(LocalDateTime.now());
        quiz.setEndedAt(null);
        quizRepository.save(quiz);
    }

    /**
     * End an active quiz.
     */
    @Transactional
    public void endQuiz(Long quizId) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz not found with ID: " + quizId));

        if (!quiz.getIsActive()) {
            throw new BadRequestException("Quiz is not currently active");
        }

        quiz.setIsActive(false);
        quiz.setEndedAt(LocalDateTime.now());
        quizRepository.save(quiz);
    }

    /**
     * Get quiz results summary.
     */
    @Transactional(readOnly = true)
    public QuizResultsSummaryDTO getQuizResults(Long quizId) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz not found with ID: " + quizId));

        ConcurrentHashMap<Long, QuizResponseEntry> responses = quizResponses.getOrDefault(quizId,
                new ConcurrentHashMap<>());

        int totalResponses = responses.size();
        int correctResponses = 0;
        int optionACount = 0;
        int optionBCount = 0;
        int optionCCount = 0;
        int optionDCount = 0;

        for (QuizResponseEntry entry : responses.values()) {
            if (entry.isCorrect())
                correctResponses++;
            switch (entry.selectedAnswer()) {
                case A -> optionACount++;
                case B -> optionBCount++;
                case C -> optionCCount++;
                case D -> optionDCount++;
            }
        }

        return new QuizResultsSummaryDTO(
                totalResponses,
                correctResponses,
                optionACount,
                optionBCount,
                optionCCount,
                optionDCount,
                quiz.getCorrectAnswer());
    }

    /**
     * Delete a quiz.
     */
    @Transactional
    public void deleteQuiz(Long quizId) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz not found with ID: " + quizId));

        if (quiz.getIsActive()) {
            throw new BadRequestException("Cannot delete an active quiz. End it first.");
        }

        // Clean up in-memory responses
        quizResponses.remove(quizId);
        quizRepository.delete(quiz);
    }

    // ============ STUDENT OPERATIONS ============

    /**
     * Get the active quiz for a meeting (student view).
     */
    @Transactional(readOnly = true)
    public ActiveQuizResponseDTO getActiveQuizForMeeting(String meetingId) {
        UUID uuid = parseUUID(meetingId);
        Quiz activeQuiz = quizRepository.findByMeetingMeetingIdAndIsActiveTrue(uuid)
                .orElseThrow(() -> new ResourceNotFoundException("No active quiz found for this meeting"));

        // Calculate remaining time
        int timeRemainingSeconds = calculateTimeRemaining(activeQuiz);
        if (timeRemainingSeconds <= 0) {
            // Auto-end the quiz if time has expired
            activeQuiz.setIsActive(false);
            activeQuiz.setEndedAt(LocalDateTime.now());
            quizRepository.save(activeQuiz);
            throw new ResourceNotFoundException("No active quiz found for this meeting");
        }

        return new ActiveQuizResponseDTO(
                activeQuiz.getId(),
                activeQuiz.getQuestion(),
                activeQuiz.getOptionA(),
                activeQuiz.getOptionB(),
                activeQuiz.getOptionC(),
                activeQuiz.getOptionD(),
                timeRemainingSeconds);
    }

    /**
     * Submit a student's response to a quiz.
     */
    @Transactional
    public void submitResponse(Long quizId, Long studentId, CorrectAnswer selectedAnswer) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz not found with ID: " + quizId));

        if (!quiz.getIsActive()) {
            throw new BadRequestException("This quiz is no longer active");
        }

        // Check if time has expired
        int timeRemaining = calculateTimeRemaining(quiz);
        if (timeRemaining <= 0) {
            throw new BadRequestException("Time has expired for this quiz");
        }

        // Get or create the response map for this quiz
        ConcurrentHashMap<Long, QuizResponseEntry> responses = quizResponses.computeIfAbsent(quizId,
                k -> new ConcurrentHashMap<>());

        // Check if student has already responded
        if (responses.containsKey(studentId)) {
            throw new BadRequestException("You have already submitted a response for this quiz");
        }

        // Record the response
        boolean isCorrect = selectedAnswer == quiz.getCorrectAnswer();
        responses.put(studentId, new QuizResponseEntry(studentId, selectedAnswer, LocalDateTime.now(), isCorrect));
    }

    // ============ HELPER METHODS ============

    private UUID parseUUID(String meetingId) {
        try {
            return UUID.fromString(meetingId);
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid meeting ID format");
        }
    }

    private int calculateTimeRemaining(Quiz quiz) {
        if (quiz.getLaunchedAt() == null)
            return 0;
        LocalDateTime expiresAt = quiz.getLaunchedAt().plusSeconds(quiz.getTimeLimitSeconds());
        long secondsRemaining = Duration.between(LocalDateTime.now(), expiresAt).getSeconds();
        return (int) Math.max(0, secondsRemaining);
    }

    private QuizResponseDTO mapToQuizResponseDTO(Quiz quiz) {
        return new QuizResponseDTO(
                quiz.getId(),
                quiz.getQuestion(),
                quiz.getOptionA(),
                quiz.getOptionB(),
                quiz.getOptionC(),
                quiz.getOptionD(),
                quiz.getCorrectAnswer(),
                quiz.getTimeLimitSeconds(),
                quiz.getIsActive(),
                quiz.getCreatedAt());
    }
}
