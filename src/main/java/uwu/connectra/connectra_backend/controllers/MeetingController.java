package uwu.connectra.connectra_backend.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import uwu.connectra.connectra_backend.dtos.AgoraTokenResponseDTO;
import uwu.connectra.connectra_backend.dtos.ApiResponse;
import uwu.connectra.connectra_backend.dtos.AttendanceReportResponseDTO;
import uwu.connectra.connectra_backend.dtos.meeting.CreateMeetingRequestDTO;
import uwu.connectra.connectra_backend.dtos.meeting.MeetingResponseDTO;
import uwu.connectra.connectra_backend.dtos.meeting.UpdateMeetingRequestDTO;
import uwu.connectra.connectra_backend.dtos.quiz.CreateQuizRequestDTO;
import uwu.connectra.connectra_backend.dtos.quiz.QuizResponseDTO;
import uwu.connectra.connectra_backend.dtos.quiz.QuizResultsSummaryDTO;
import uwu.connectra.connectra_backend.services.MeetingService;
import uwu.connectra.connectra_backend.services.QuizService;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/meeting")
@Tag(name = "Meeting Controller", description = "Endpoints for managing meetings")
public class MeetingController {
    private final MeetingService meetingService;
    private final QuizService quizService;

    // Create Meeting
    @PostMapping
    @PreAuthorize("hasAnyRole('LECTURER')")
    @Operation(summary = "Create a new meeting")
    public ResponseEntity<ApiResponse<MeetingResponseDTO>> createMeeting(@RequestBody @Validated CreateMeetingRequestDTO request) {

        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse<>(
                true,
                "Meeting created successfully.",
                meetingService.createMeeting(request)
        ));
    }

    // Get all Meetings (created by the authenticated lecturer)
    @PreAuthorize("hasAnyRole('LECTURER', 'ADMIN')")
    @GetMapping("/lecturer")
    @Operation(summary = "Get all meetings created by the authenticated lecturer")
    public ResponseEntity<ApiResponse<List<MeetingResponseDTO>>> getAllMeetings() {
        return ResponseEntity.status(HttpStatus.OK).body((new ApiResponse<>(
                        true,
                        "Meetings retrieved successfully.",
                        meetingService.getAllMeetings()
                )
                )
        );
    }

    // Get Meeting by its ID
    @PreAuthorize("hasAnyRole('LECTURER')")
    @GetMapping("/{meetingId}")
    @Operation(summary = "Get meeting details by its ID")
    public ResponseEntity<ApiResponse<MeetingResponseDTO>> getMeetingById(@PathVariable String meetingId) {
        return ResponseEntity.status(HttpStatus.OK).body((new ApiResponse<>(
                true,
                "Meeting retrieved successfully.",
                meetingService.getMeetingById(meetingId)
        )
        ));
    }

    // Update Meeting by its ID
    @PreAuthorize("hasAnyRole('LECTURER')")
    @PutMapping("/{meetingId}")
    @Operation(summary = "Update meeting details by its ID")
    public ResponseEntity<ApiResponse<MeetingResponseDTO>> updateMeetingById(@PathVariable String meetingId, @RequestBody @Validated UpdateMeetingRequestDTO request) {
        return ResponseEntity.status(HttpStatus.OK).body((new ApiResponse<>(
                true,
                "Meeting updated successfully.",
                meetingService.updateMeetingById(meetingId, request)
        )
        ));
    }

    // Cancel Meeting by its ID
    @PreAuthorize("hasAnyRole('LECTURER')")
    @PutMapping("/{meetingId}/cancel")
    @Operation(summary = "Cancel meeting by its ID")
    public ResponseEntity<ApiResponse<MeetingResponseDTO>> cancelMeetingById(@PathVariable String meetingId) {
        return ResponseEntity.status(HttpStatus.OK).body((new ApiResponse<>(
                true,
                "Meeting canceled successfully.",
                meetingService.cancelMeetingById(meetingId)
        )
        ));
    }

    // Join Meeting by its ID (Generate Agora Token)
    @PreAuthorize("hasAnyRole('LECTURER', 'STUDENT')")
    @PostMapping("/{meetingId}/join")
    @Operation(summary = "Generate Agora token to join meeting by its ID")
    public ResponseEntity<ApiResponse<AgoraTokenResponseDTO>> joinMeetingById(@PathVariable String meetingId) {
        return ResponseEntity.status(HttpStatus.OK).body((new ApiResponse<>(
                        true,
                        "Joined meeting successfully.",
                        meetingService.joinMeeting(meetingId)
                )
                )
        );
    }

    // Leave Meeting by its ID
    @PreAuthorize("hasAnyRole('LECTURER', 'STUDENT')")
    @PutMapping("/{meetingId}/leave")
    @Operation(summary = "Leave meeting by its ID")
    public ResponseEntity<ApiResponse<String>> leaveMeetingById(@PathVariable String meetingId) {
        return ResponseEntity.status(HttpStatus.OK).body((new ApiResponse<>(
                true,
                "Left meeting successfully.",
                meetingService.leaveMeeting(meetingId)
        )
        ));
    }


    // Stop Meeting by its ID
    @PreAuthorize("hasAnyRole('LECTURER')")
    @PutMapping("/{meetingId}/stop")
    @Operation(summary = "Stop meeting by its ID")
    public ResponseEntity<ApiResponse<MeetingResponseDTO>> stopMeeting(@PathVariable String meetingId) {
        return ResponseEntity.status(HttpStatus.OK).body((new ApiResponse<>(
                true,
                "Meeting stopped successfully.",
                meetingService.stopMeeting(meetingId)
        )
        ));
    }

    // ===== ATTENDANCE REPORTS ENDPOINTS =====
    // Get Attendance Report data for a Meeting by its ID
    @PreAuthorize("hasAnyRole('LECTURER')")
    @GetMapping("/{meetingId}/attendance")
    @Operation(summary = "Get attendance report for a meeting by its ID")
    public ResponseEntity<ApiResponse<AttendanceReportResponseDTO>> getAttendanceReport(@PathVariable String meetingId) {
        AttendanceReportResponseDTO report = meetingService.generateAttendanceReport(meetingId);
        return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse<>(
                true,
                "Attendance report data generated successfully.",
                report
        ));
    }


    // QUIZ MANAGEMENT ENDPOINTS

    @PreAuthorize("hasRole('LECTURER')")
    @PostMapping("/{meetingId}/quizzes")
    @Operation(summary = "Create a new quiz for a meeting")
    public ResponseEntity<ApiResponse<QuizResponseDTO>> createQuiz(
            @PathVariable String meetingId,
            @RequestBody @Validated CreateQuizRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse<>(
                true, "Quiz created successfully",
                quizService.createQuiz(meetingId, request)));
    }

    @PreAuthorize("hasRole('LECTURER')")
    @GetMapping("/{meetingId}/quizzes")
    @Operation(summary = "Get all quizzes for a meeting")
    public ResponseEntity<ApiResponse<List<QuizResponseDTO>>> getQuizzes(@PathVariable String meetingId) {
        return ResponseEntity.ok(new ApiResponse<>(
                true, "Quizzes retrieved successfully",
                quizService.getQuizzesForMeeting(meetingId)));
    }

    @PreAuthorize("hasRole('LECTURER')")
    @DeleteMapping("/quizzes/{quizId}")
    @Operation(summary = "Delete a quiz")
    public ResponseEntity<ApiResponse<Void>> deleteQuiz(@PathVariable Long quizId) {
        quizService.deleteQuiz(quizId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Quiz deleted successfully", null));
    }

    @PreAuthorize("hasRole('LECTURER')")
    @PostMapping("/quizzes/{quizId}/launch")
    @Operation(summary = "Launch a quiz (start timer)")
    public ResponseEntity<ApiResponse<QuizResponseDTO>> launchQuiz(@PathVariable Long quizId) {
        return ResponseEntity.ok(new ApiResponse<>(
                true, "Quiz launched successfully",
                quizService.launchQuiz(quizId)));
    }

    @PreAuthorize("hasRole('LECTURER')")
    @PostMapping("/quizzes/{quizId}/end")
    @Operation(summary = "End an active quiz")
    public ResponseEntity<ApiResponse<QuizResponseDTO>> endQuiz(@PathVariable Long quizId) {
        return ResponseEntity.ok(new ApiResponse<>(
                true, "Quiz ended successfully",
                quizService.endQuiz(quizId)));
    }

    @PreAuthorize("hasRole('LECTURER')")
    @GetMapping("/quizzes/{quizId}/results")
    @Operation(summary = "Get results for a quiz")
    public ResponseEntity<ApiResponse<QuizResultsSummaryDTO>> getQuizResults(@PathVariable Long quizId) {
        return ResponseEntity.ok(new ApiResponse<>(
                true, "Quiz results retrieved successfully",
                quizService.getQuizResults(quizId)));
    }
}