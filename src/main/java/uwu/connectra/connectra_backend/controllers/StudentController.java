package uwu.connectra.connectra_backend.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uwu.connectra.connectra_backend.dtos.ApiResponse;
import uwu.connectra.connectra_backend.dtos.meeting.MeetingResponseDTO;
import uwu.connectra.connectra_backend.entities.Student;
import uwu.connectra.connectra_backend.entities.User;
import uwu.connectra.connectra_backend.repositories.UserRepository;
import uwu.connectra.connectra_backend.services.MeetingService;

import java.util.List;

@RestController
@RequestMapping("/api/student")
@Tag(name = "Student Controller", description = "Endpoints for student-specific operations")
public class StudentController {

    private final MeetingService meetingService;
    private final UserRepository userRepository;

    public StudentController(MeetingService meetingService, UserRepository userRepository) {
        this.meetingService = meetingService;
        this.userRepository = userRepository;
    }

    @PreAuthorize("hasAnyRole('STUDENT', 'ADMIN')")
    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<String>> studentDashboard() {
        return ResponseEntity.ok(new ApiResponse<>(true, "Welcome to the Student Dashboard!", null));
    }

    //NEW ENDPOINT: Get Student Meetings
    @PreAuthorize("hasRole('STUDENT')")
    @GetMapping("/meetings")
    @Operation(summary = "Get all scheduled/live meetings for the student's degree and batch")
    public ResponseEntity<ApiResponse<List<MeetingResponseDTO>>> getMyMeetings(Authentication authentication) {
        // 1. Get logged-in user email
        String email = authentication.getName();

        // 2. Fetch User and cast to Student to get academic details
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Student profile not found"));

        if (!(user instanceof Student student)) {
            throw new RuntimeException("Authenticated user is not a student");
        }

        // 3. Call service with student's Degree and Batch
        List<MeetingResponseDTO> meetings = meetingService.getStudentMeetings(
                student.getDegree(),
                student.getBatch()
        );

        return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Meetings retrieved successfully.",
                meetings
        ));
    }

}
