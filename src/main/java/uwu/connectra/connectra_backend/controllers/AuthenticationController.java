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
import uwu.connectra.connectra_backend.dtos.auth.*;
import uwu.connectra.connectra_backend.services.AuthenticationService;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication Controller", description = "Endpoints for user authentication operations")
public class AuthenticationController {
        private final AuthenticationService authenticationService;

        // User Registration (creates user with PENDING_VERIFICATION status and sends
        // OTP)
        @PostMapping("/register")
        @Operation(summary = "Register a new user", description = "Creates a new user account and sends OTP to email for verification")
        public ResponseEntity<ApiResponse<RegisterResponseDTO>> register(
                        @Valid @RequestBody UserRegisterRequestDTO request) {
                RegisterResponseDTO response = authenticationService.registerUser(request);
                return ResponseEntity.status(HttpStatus.CREATED)
                                .body(new ApiResponse<>(true, "Registration initiated. Please verify your email.",
                                                response));
        }

        // Verify Email OTP
        @PostMapping("/verify-email")
        @Operation(summary = "Verify email with OTP", description = "Verifies the email using the OTP sent during registration")
        public ResponseEntity<ApiResponse<UserAuthResponseDTO>> verifyEmail(
                        @Valid @RequestBody VerifyEmailRequestDTO request,
                        HttpServletResponse httpServletResponse) {
                UserAuthResponseDTO response = authenticationService.verifyEmailAndActivateUser(
                                request.getEmail(), request.getOtp(), httpServletResponse);
                return ResponseEntity.status(HttpStatus.OK)
                                .body(new ApiResponse<>(true, "Email verified successfully! You are now logged in.",
                                                response));
        }

        // Resend OTP
        @PostMapping("/resend-otp")
        @Operation(summary = "Resend OTP", description = "Resends the OTP verification code to the user's email")
        public ResponseEntity<ApiResponse<String>> resendOtp(
                        @Valid @RequestBody ResendOtpRequestDTO request) {
                authenticationService.resendOtp(request.getEmail());
                return ResponseEntity.status(HttpStatus.OK)
                                .body(new ApiResponse<>(true, "A new verification code has been sent to your email.",
                                                null));
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
                        @CookieValue("refreshToken") String refreshToken) {
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
