package uwu.connectra.connectra_backend.controllers;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uwu.connectra.connectra_backend.dtos.LogoutResponseDTO;
import uwu.connectra.connectra_backend.dtos.UserAuthResponseDTO;
import uwu.connectra.connectra_backend.dtos.UserLoginRequestDTO;
import uwu.connectra.connectra_backend.dtos.UserRegisterRequestDTO;
import uwu.connectra.connectra_backend.services.AuthenticationService;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthenticationController {
    private final AuthenticationService authenticationService;

    // User Registration
    @PostMapping("/register")
    public ResponseEntity<UserAuthResponseDTO> register(
            @RequestBody UserRegisterRequestDTO request,
            HttpServletResponse httpServletResponse) {
        UserAuthResponseDTO response = authenticationService.registerUser(request, httpServletResponse);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // User Login
    @PostMapping("/login")
    public ResponseEntity<UserAuthResponseDTO> login(
            @RequestBody UserLoginRequestDTO request,
            HttpServletResponse httpServletResponse) {
        UserAuthResponseDTO response = authenticationService.loginUser(request, httpServletResponse);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    // Refresh Token
    @PostMapping("/refresh-token")
    public ResponseEntity<UserAuthResponseDTO> refreshToken(
            @CookieValue("refreshToken") String refreshToken,
            HttpServletResponse httpServletResponse
    ) {
        UserAuthResponseDTO response = authenticationService.refreshAccessToken(refreshToken, httpServletResponse);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    // User Logout
    @PostMapping("/logout")
    public ResponseEntity<LogoutResponseDTO> logout(
            HttpServletResponse httpServletResponse) {
        authenticationService.logoutUser(httpServletResponse);
        LogoutResponseDTO response = new LogoutResponseDTO(
                "User logged out successfully",
                true
        );
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
