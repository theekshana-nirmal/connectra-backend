package uwu.connectra.connectra_backend.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uwu.connectra.connectra_backend.dtos.ApiResponse;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<String>> adminDashboard() {
        return ResponseEntity.ok(new ApiResponse<>(true, "Welcome to the Admin Dashboard!", null));
    }

    // LECTURER ACCOUNT MANAGEMENT
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/lecturers")
    public ResponseEntity<ApiResponse<String>> createLecturerAccount() {
        // Logic to create a lecturer account
        return ResponseEntity.ok(new ApiResponse<>(true, "Lecturer account created successfully.", null));
    }
}
