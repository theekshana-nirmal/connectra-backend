package uwu.connectra.connectra_backend.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import uwu.connectra.connectra_backend.dtos.ApiResponse;
import uwu.connectra.connectra_backend.dtos.LecturerResponseDTO;
import uwu.connectra.connectra_backend.dtos.UserRegisterRequestDTO;
import uwu.connectra.connectra_backend.services.AuthenticationService;
import uwu.connectra.connectra_backend.services.UserService;

import java.util.List;

@RestController
@RequestMapping("/api/lecturers")
@Tag(name = "Lecturer Controller", description = "Endpoints for lecturer-specific operations")
@RequiredArgsConstructor
public class LecturerController {
    private final UserService userService;
    private final AuthenticationService authenticationService;

    @PreAuthorize("hasAnyRole('LECTURER', 'ADMIN')")
    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<String>> studentDashboard() {
        return ResponseEntity.ok(new ApiResponse<>(true, "Welcome to the Lecturer Dashboard!", null));
    }

    // Create a Lecturer Account
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    @Operation(summary = "Create a new lecturer account")
    public ResponseEntity<ApiResponse<LecturerResponseDTO>> createLecturerAccount(
            @Validated @RequestBody UserRegisterRequestDTO request) {
        // Logic to create a lecturer account
        LecturerResponseDTO response = authenticationService.createLecturer(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(
                                true,
                                "Lecturer account created successfully.",
                                response
                        )
                );
    }

    // Get all lecturers
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    @Operation(summary = "Get all lecturer accounts")
    public ResponseEntity<ApiResponse<List<LecturerResponseDTO>>> getAllLecturers() {
        var lecturers = userService.getAllLecturers();
        return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Lecturer accounts retrieved successfully.",
                lecturers
        ));
    }
}
