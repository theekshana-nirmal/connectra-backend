package uwu.connectra.connectra_backend.controllers;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uwu.connectra.connectra_backend.dtos.ApiResponse;

@RestController
@RequestMapping("/api/lecturer")
@Tag(name = "Lecturer Controller", description = "Endpoints for lecturer-specific operations")
public class LecturerController {
    @PreAuthorize("hasAnyRole('LECTURER', 'ADMIN')")
    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<String>> studentDashboard() {
        return ResponseEntity.ok(new ApiResponse<>(true, "Welcome to the Lecturer Dashboard!", null));
    }
}
