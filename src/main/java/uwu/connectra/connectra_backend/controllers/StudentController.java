package uwu.connectra.connectra_backend.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import uwu.connectra.connectra_backend.dtos.ApiResponse;
import uwu.connectra.connectra_backend.dtos.StudentAttendanceHistoryResponseDTO;
import uwu.connectra.connectra_backend.dtos.meeting.MeetingResponseDTO;
import uwu.connectra.connectra_backend.dtos.quiz.ActiveQuizDTO;
import uwu.connectra.connectra_backend.dtos.quiz.SubmitQuizResponseDTO;
import uwu.connectra.connectra_backend.entities.AttendanceStatus;
import uwu.connectra.connectra_backend.services.AttendanceService;
import uwu.connectra.connectra_backend.services.MeetingService;
import uwu.connectra.connectra_backend.services.QuizService;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/student")
@Tag(name = "Student Controller", description = "Endpoints for student-specific operations")
public class StudentController {
    private final MeetingService meetingService;
    private final AttendanceService attendanceService;
    private final QuizService quizService;

    // GET ALL SCHEDULED/LIVE MEETINGS FOR CURRENT STUDENT'S DEGREE AND BATCH
    @PreAuthorize("hasRole('STUDENT')")
    @GetMapping("/meetings")
    @Operation(summary = "Get all scheduled/live meetings for the current student's degree and batch")
    public ResponseEntity<ApiResponse<List<MeetingResponseDTO>>> getMyMeetings() {
        List<MeetingResponseDTO> meetings = meetingService.getStudentMeetings();

        return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Meetings retrieved successfully.",
                meetings));
    }

    // GET ATTENDANCE HISTORY FOR CURRENT STUDENT
    @PreAuthorize("hasRole('STUDENT')")
    @GetMapping("/attendance/history")
    @Operation(
            summary = "Get attendance history for the current student",
            description = "Returns all completed meetings for the student's degree/batch with detailed attendance information. "
                    +
                    "Includes both attended and missed meetings. Supports filtering by attendance status."
    )
    public ResponseEntity<ApiResponse<List<StudentAttendanceHistoryResponseDTO>>> getMyAttendanceHistory(
            @RequestParam(required = false) AttendanceStatus status) {
        List<StudentAttendanceHistoryResponseDTO> attendanceHistory = attendanceService
                .getStudentAttendanceHistory(status);
        return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Attendance history retrieved successfully.",
                attendanceHistory));
    }

    // QUIZ PARTICIPATION ENDPOINTS

    @PreAuthorize("hasRole('STUDENT')")
    @GetMapping("/meetings/{meetingId}/quiz/active")
    @Operation(summary = "Get the currently active quiz for a meeting")
    public ResponseEntity<ApiResponse<ActiveQuizDTO>> getActiveQuiz(@PathVariable String meetingId) {
        return ResponseEntity.ok(new ApiResponse<>(
                true, "Active quiz retrieved successfully",
                quizService.getActiveQuizForStudent(meetingId)));
    }

    @PreAuthorize("hasRole('STUDENT')")
    @PostMapping("/quizzes/{quizId}/respond")
    @Operation(summary = "Submit an answer for a quiz")
    public ResponseEntity<ApiResponse<Void>> submitQuizResponse(
            @PathVariable Long quizId,
            @RequestBody @Validated SubmitQuizResponseDTO request) {
        quizService.submitResponse(quizId, request);
        return ResponseEntity.ok(new ApiResponse<>(true, "Response submitted successfully", null));
    }
}