package uwu.connectra.connectra_backend.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import uwu.connectra.connectra_backend.dtos.ApiResponse;
import uwu.connectra.connectra_backend.dtos.LecturerCreateResponseDTO;
import uwu.connectra.connectra_backend.dtos.UserRegisterRequestDTO;
import uwu.connectra.connectra_backend.services.AuthenticationService;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {
    private final AuthenticationService authenticationService;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<String>> adminDashboard() {
        return ResponseEntity.ok(new ApiResponse<>(true, "Welcome to the Admin Dashboard!", null));
    }

    // === LECTURER ACCOUNT MANAGEMENT ===
    // Create Lecturer Account
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/lecturers")
    public ResponseEntity<ApiResponse<LecturerCreateResponseDTO>> createLecturerAccount(
            @Validated @RequestBody UserRegisterRequestDTO request) {
        // Logic to create a lecturer account
        LecturerCreateResponseDTO response = authenticationService.createLecturer(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "Lecturer account created successfully.", response));
    }
}