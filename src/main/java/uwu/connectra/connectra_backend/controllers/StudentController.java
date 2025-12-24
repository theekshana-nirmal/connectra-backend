package uwu.connectra.connectra_backend.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uwu.connectra.connectra_backend.dtos.ApiResponse;
import uwu.connectra.connectra_backend.dtos.StudentAttendanceHistoryResponseDTO;
import uwu.connectra.connectra_backend.dtos.meeting.MeetingResponseDTO;
import uwu.connectra.connectra_backend.entities.AttendanceStatus;
import uwu.connectra.connectra_backend.services.AttendanceService;
import uwu.connectra.connectra_backend.services.MeetingService;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/student")
@Tag(name = "Student Controller", description = "Endpoints for student-specific operations")
public class StudentController {
    private final MeetingService meetingService;
    private final AttendanceService attendanceService;

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
}
