package uwu.connectra.connectra_backend.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uwu.connectra.connectra_backend.dtos.ApiResponse;
import uwu.connectra.connectra_backend.dtos.LogoutResponseDTO;
import uwu.connectra.connectra_backend.dtos.UserAuthResponseDTO;
import uwu.connectra.connectra_backend.dtos.UserLoginRequestDTO;
import uwu.connectra.connectra_backend.dtos.UserRegisterRequestDTO;
import uwu.connectra.connectra_backend.services.AuthenticationService;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication Controller", description = "Endpoints for user authentication operations")
public class AuthenticationController {
    private final AuthenticationService authenticationService;

    // User Registration
    @PostMapping("/register")
    @Operation(summary = "Register a new user")
    public ResponseEntity<ApiResponse<UserAuthResponseDTO>> register(
            @Valid @RequestBody UserRegisterRequestDTO request,
            HttpServletResponse httpServletResponse) {
        UserAuthResponseDTO response = authenticationService.registerUser(request, httpServletResponse);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "User registered successfully", response));
    }

    // User Login
    @PostMapping("/login")
    @Operation(summary = "User login")
    public ResponseEntity<ApiResponse<UserAuthResponseDTO>> login(
            @Valid @RequestBody UserLoginRequestDTO request,
            HttpServletResponse httpServletResponse) {
        UserAuthResponseDTO response = authenticationService.loginUser(request, httpServletResponse);
        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponse<>(true, "Login successful", response));
    }

    // Refresh Token
    @PostMapping("/refresh-token")
    @Operation(summary = "Refresh 'access token' using refresh token")
    public ResponseEntity<ApiResponse<UserAuthResponseDTO>> refreshToken(
            @CookieValue("refreshToken") String refreshToken,
            HttpServletResponse httpServletResponse) {
        UserAuthResponseDTO response = authenticationService.refreshAccessToken(refreshToken);
        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponse<>(true, "Token refreshed successfully", response));
    }

    // User Logout
    @PostMapping("/logout")
    @Operation(summary = "User logout")
    public ResponseEntity<ApiResponse<LogoutResponseDTO>> logout(
            HttpServletResponse httpServletResponse) {
        authenticationService.logoutUser(httpServletResponse);
        LogoutResponseDTO response = new LogoutResponseDTO(
                "User logged out successfully",
                true);
        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponse<>(true, "Logout successful", response));
    }
}
