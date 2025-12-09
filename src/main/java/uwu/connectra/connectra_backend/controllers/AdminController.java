package uwu.connectra.connectra_backend.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import uwu.connectra.connectra_backend.dtos.ApiResponse;
import uwu.connectra.connectra_backend.services.AuthenticationService;
import uwu.connectra.connectra_backend.services.UserService;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Tag(name = "Admin Controller", description = "Endpoints for admin-specific operations")
public class AdminController {
    private final AuthenticationService authenticationService;
    private final UserService userService;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<String>> adminDashboard() {
        return ResponseEntity.ok(new ApiResponse<>(true, "Welcome to the Admin Dashboard!", null));
    }

    // === USER MANAGEMENT ===
    // Delete User Account
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/users/{userId}")
    @Operation(summary = "Delete a user account by user ID")
    public ResponseEntity<ApiResponse<String>> deleteUserAccount(
            @PathVariable Long userId) {
        userService.deleteUserById(userId);
        return ResponseEntity.noContent().build();
    }
}