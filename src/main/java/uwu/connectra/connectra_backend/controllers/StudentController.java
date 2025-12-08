package uwu.connectra.connectra_backend.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uwu.connectra.connectra_backend.dtos.ApiResponse;

@RestController
@RequestMapping("/api/student")
public class StudentController {
    @PreAuthorize("hasAnyRole('STUDENT', 'ADMIN')")
    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<String>> studentDashboard() {
        return ResponseEntity.ok(new ApiResponse<>(true, "Welcome to the Student Dashboard!", null));
    }
}
