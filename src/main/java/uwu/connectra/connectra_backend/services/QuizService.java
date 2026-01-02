package uwu.connectra.connectra_backend.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uwu.connectra.connectra_backend.config.QuizResponseStorage;
import uwu.connectra.connectra_backend.dtos.quiz.*;
import uwu.connectra.connectra_backend.entities.*;
import uwu.connectra.connectra_backend.exceptions.*;
import uwu.connectra.connectra_backend.repositories.MeetingRepository;
import uwu.connectra.connectra_backend.repositories.QuizRepository;
import uwu.connectra.connectra_backend.repositories.StudentRepository;
import uwu.connectra.connectra_backend.utils.CurrentUserProvider;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class QuizService {

    private final QuizRepository quizRepository;
    private final MeetingRepository meetingRepository;
    private final StudentRepository studentRepository;
    private final QuizResponseStorage responseStorage;
    private final CurrentUserProvider currentUserProvider;

    //  LECTURER: CREATE QUIZ
    @Transactional
    public QuizResponseDTO createQuiz(String meetingId, CreateQuizRequestDTO dto) {
        Lecturer lecturer = currentUserProvider.getCurrentUserAs(Lecturer.class);
        Meeting meeting = getMeetingById(meetingId);

        validateMeetingOwnership(meeting, lecturer);
        validateQuizOptions(dto);

        Quiz quiz = new Quiz();
        quiz.setMeeting(meeting);
        quiz.setQuestionText(dto.getQuestionText());
        quiz.setOptionA(dto.getOptionA());
        quiz.setOptionB(dto.getOptionB());
        quiz.setOptionC(dto.getOptionC());
        quiz.setOptionD(dto.getOptionD());
        quiz.setCorrectAnswer(Character.toUpperCase(dto.getCorrectAnswer()));
        quiz.setTimeLimitSeconds(dto.getTimeLimitSeconds());
        quiz.setActive(false);

        Quiz saved = quizRepository.save(quiz);
        return mapToDTO(saved);
    }

    // LECTURER: LIST QUIZZES
    public List<QuizResponseDTO> getQuizzesForMeeting(String meetingId) {
        Lecturer lecturer = currentUserProvider.getCurrentUserAs(Lecturer.class);
        Meeting meeting = getMeetingById(meetingId);
        validateMeetingOwnership(meeting, lecturer); // Lecturer can only see their quizzes

        return quizRepository.findByMeetingOrderByLaunchedAtDesc(meeting).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    //  LECTURER: DELETE QUIZ
    @Transactional
    public void deleteQuiz(Long quizId) {
        Lecturer lecturer = currentUserProvider.getCurrentUserAs(Lecturer.class);
        Quiz quiz = getQuizById(quizId);
        validateMeetingOwnership(quiz.getMeeting(), lecturer);

        if (quiz.isActive()) {
            throw new IllegalArgumentException("Cannot delete an active quiz.");
        }
        quizRepository.delete(quiz);
    }

    // LECTURER: LAUNCH QUIZ
    @Transactional
    public QuizResponseDTO launchQuiz(Long quizId) {
        Lecturer lecturer = currentUserProvider.getCurrentUserAs(Lecturer.class);
        Quiz quiz = getQuizById(quizId);
        Meeting meeting = quiz.getMeeting();

        validateMeetingOwnership(meeting, lecturer);

        if (meeting.getStatus() != MeetingStatus.LIVE) {
            throw new IllegalStateException("Quiz can only be launched during a LIVE meeting.");
        }

        // Check if another quiz is active
        if (quizRepository.findByMeetingAndIsActiveTrue(meeting).isPresent()) {
            throw new IllegalStateException("Another quiz is currently active in this meeting.");
        }

        quiz.setActive(true);
        quiz.setLaunchedAt(LocalDateTime.now());
        quiz.setEndedAt(null); // Reset ended at if re-launching

        // Clear old responses for this quiz from memory
        responseStorage.clearResponsesForQuiz(quizId);

        Quiz saved = quizRepository.save(quiz);
        return mapToDTO(saved);
    }

    //  LECTURER: END QUIZ
    @Transactional
    public QuizResponseDTO endQuiz(Long quizId) {
        Lecturer lecturer = currentUserProvider.getCurrentUserAs(Lecturer.class);
        Quiz quiz = getQuizById(quizId);
        validateMeetingOwnership(quiz.getMeeting(), lecturer);

        if (!quiz.isActive()) {
            throw new IllegalStateException("Quiz is not currently active.");
        }

        quiz.setActive(false);
        quiz.setEndedAt(LocalDateTime.now());

        Quiz saved = quizRepository.save(quiz);
        return mapToDTO(saved);
    }

    // STUDENT: GET ACTIVE QUIZ
    public ActiveQuizDTO getActiveQuizForStudent(String meetingId) {
        Student student = currentUserProvider.getCurrentUserAs(Student.class);
        Meeting meeting = getMeetingById(meetingId);

        validateStudentAccess(meeting, student);

        Quiz activeQuiz = quizRepository.findByMeetingAndIsActiveTrue(meeting)
                .orElseThrow(() -> new RuntimeException("No active quiz found for this meeting."));

        // Calculate time remaining
        long secondsElapsed = Duration.between(activeQuiz.getLaunchedAt(), LocalDateTime.now()).getSeconds();
        long timeRemaining = activeQuiz.getTimeLimitSeconds() - secondsElapsed;

        if (timeRemaining <= 0) {
            // Auto-close logic could be triggered here or lazily
            throw new RuntimeException("Quiz time has expired.");
        }

        ActiveQuizDTO dto = new ActiveQuizDTO();
        dto.setId(activeQuiz.getId());
        dto.setQuestionText(activeQuiz.getQuestionText());
        dto.setOptionA(activeQuiz.getOptionA());
        dto.setOptionB(activeQuiz.getOptionB());
        dto.setOptionC(activeQuiz.getOptionC());
        dto.setOptionD(activeQuiz.getOptionD());
        dto.setTimeLimitSeconds(activeQuiz.getTimeLimitSeconds());
        dto.setLaunchedAt(activeQuiz.getLaunchedAt());
        dto.setTimeRemainingSeconds(timeRemaining);

        return dto;
    }

    // STUDENT: SUBMIT RESPONSE
    public void submitResponse(Long quizId, SubmitQuizResponseDTO dto) {
        Student student = currentUserProvider.getCurrentUserAs(Student.class);
        Quiz quiz = getQuizById(quizId);

        if (!quiz.isActive()) {
            throw new IllegalStateException("Quiz is no longer active.");
        }

        // Double check time validity
        long secondsElapsed = Duration.between(quiz.getLaunchedAt(), LocalDateTime.now()).getSeconds();
        if (secondsElapsed > quiz.getTimeLimitSeconds()) {
            throw new IllegalStateException("Time limit exceeded.");
        }

        // Validate Answer Char
        Character answer = Character.toUpperCase(dto.getSelectedAnswer());
        if (!Arrays.asList('A', 'B', 'C', 'D').contains(answer)) {
            throw new IllegalArgumentException("Invalid answer option.");
        }

        responseStorage.addResponse(quizId, student.getId(), answer);
    }

    // LECTURER: GET RESULTS
    public QuizResultsSummaryDTO getQuizResults(Long quizId) {
        Lecturer lecturer = currentUserProvider.getCurrentUserAs(Lecturer.class);
        Quiz quiz = getQuizById(quizId);
        validateMeetingOwnership(quiz.getMeeting(), lecturer);

        List<QuizResponseStorage.QuizResponse> responses = responseStorage.getResponsesForQuiz(quizId)
                .toList();

        long totalResponses = responses.size();
        long correctCount = responses.stream()
                .filter(r -> r.getSelectedAnswer().equals(quiz.getCorrectAnswer()))
                .count();
        long incorrectCount = totalResponses - correctCount;

        Map<Character, Long> breakdown = new HashMap<>();
        breakdown.put('A', responses.stream().filter(r -> r.getSelectedAnswer() == 'A').count());
        breakdown.put('B', responses.stream().filter(r -> r.getSelectedAnswer() == 'B').count());
        breakdown.put('C', responses.stream().filter(r -> r.getSelectedAnswer() == 'C').count());
        breakdown.put('D', responses.stream().filter(r -> r.getSelectedAnswer() == 'D').count());

        // Calculate Expected Students
        long expectedStudents = studentRepository.countByDegreeAndBatch(
                quiz.getMeeting().getTargetDegree(),
                quiz.getMeeting().getTargetBatch()
        );

        double responseRate = expectedStudents > 0 ? ((double) totalResponses / expectedStudents) * 100 : 0;

        QuizResultsSummaryDTO summary = new QuizResultsSummaryDTO();
        summary.setQuizId(quizId);
        summary.setQuestionText(quiz.getQuestionText());
        summary.setCorrectAnswer(quiz.getCorrectAnswer());
        summary.setTotalStudentsInMeeting(expectedStudents);
        summary.setTotalResponses(totalResponses);
        summary.setCorrectCount(correctCount);
        summary.setIncorrectCount(incorrectCount);
        summary.setResponseRate(responseRate);
        summary.setResponseBreakdown(breakdown);

        return summary;
    }
    // helper methods

    private Meeting getMeetingById(String meetingId) {
        return meetingRepository.findById(UUID.fromString(meetingId))
                .orElseThrow(() -> new MeetingNotFoundException("Meeting not found"));
    }

    private Quiz getQuizById(Long quizId) {
        return quizRepository.findById(quizId)
                .orElseThrow(() -> new RuntimeException("Quiz not found"));
    }

    private void validateMeetingOwnership(Meeting meeting, Lecturer lecturer) {
        // CORRECTED: Use != operator for primitive long comparison
        if (meeting.getCreatedBy().getId() != lecturer.getId()) {
            throw new UnauthorizedException("You do not have permission to access quizzes for this meeting.");
        }
    }

    private void validateStudentAccess(Meeting meeting, Student student) {
        if (!meeting.getTargetDegree().equalsIgnoreCase(student.getDegree()) ||
                meeting.getTargetBatch() != student.getBatch()) {
            throw new UnauthorizedException("You are not authorized to view this quiz.");
        }
    }

    private void validateQuizOptions(CreateQuizRequestDTO dto) {
        Character correct = Character.toUpperCase(dto.getCorrectAnswer());
        if (!Arrays.asList('A', 'B', 'C', 'D').contains(correct)) {
            throw new IllegalArgumentException("Correct answer must be A, B, C, or D.");
        }

        List<Integer> validTimes = Arrays.asList(30, 60, 120);
        if(!validTimes.contains(dto.getTimeLimitSeconds())) {
            throw new IllegalArgumentException("Time limit must be 30, 60 or 120 seconds.");
        }
    }

    private QuizResponseDTO mapToDTO(Quiz quiz) {
        QuizResponseDTO dto = new QuizResponseDTO();
        dto.setId(quiz.getId());
        dto.setQuestionText(quiz.getQuestionText());
        dto.setOptionA(quiz.getOptionA());
        dto.setOptionB(quiz.getOptionB());
        dto.setOptionC(quiz.getOptionC());
        dto.setOptionD(quiz.getOptionD());
        dto.setCorrectAnswer(quiz.getCorrectAnswer());
        dto.setTimeLimitSeconds(quiz.getTimeLimitSeconds());
        dto.setActive(quiz.isActive());
        dto.setLaunchedAt(quiz.getLaunchedAt());
        dto.setEndedAt(quiz.getEndedAt());
        return dto;
    }
}