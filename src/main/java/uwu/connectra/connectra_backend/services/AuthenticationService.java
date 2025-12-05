package uwu.connectra.connectra_backend.services;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import uwu.connectra.connectra_backend.dtos.UserAuthResponseDTO;
import uwu.connectra.connectra_backend.dtos.UserLoginRequestDTO;
import uwu.connectra.connectra_backend.dtos.UserRegisterRequestDTO;
import uwu.connectra.connectra_backend.entities.*;
import uwu.connectra.connectra_backend.repositories.UserRepository;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final StudentDetailsExtractorService studentDetailsExtractorService;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    // USER REGISTRATION
    public UserAuthResponseDTO registerUser(UserRegisterRequestDTO request, HttpServletResponse httpServletResponse) {
        // Check if user already exists
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("User with email " + request.getEmail() + " already exists");
        }

        User user;

        switch (request.getRole().name()) {
            case "STUDENT" -> {
                Student student = new Student();
                String studentEmail = request.getEmail();

                String studentId = studentDetailsExtractorService.extractStudentId(studentEmail);
                String degree = studentDetailsExtractorService.extractDegree(studentEmail);
                int batch = studentDetailsExtractorService.extractBatch(studentEmail);

                student.setStudentId(studentId);
                student.setDegree(degree);
                student.setBatch(batch);
                student.setRole(Role.STUDENT);

                user = student;
            }
            case "LECTURER" -> {
                user = new Lecturer();
                user.setRole(Role.LECTURER);
            }
            case "ADMIN" -> {
                user = new Admin();
                user.setRole(Role.ADMIN);
            }
            default -> throw new RuntimeException("Invalid Role Provided");
        }

        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setHashedPassword(passwordEncoder.encode(request.getPassword()));

        // Save user to the database
        User savedUser = userRepository.save(user);

        return authResponse(savedUser, httpServletResponse);
    }

    // USER LOGIN
    public UserAuthResponseDTO loginUser(UserLoginRequestDTO request, HttpServletResponse httpServletResponse)
            throws AuthenticationException {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()));
        } catch (AuthenticationException e) {
            throw new RuntimeException(e);
        }

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User with email " + request.getEmail() + " not found"));

        return authResponse(user, httpServletResponse);
    }

    // USER LOGOUT
    public void logoutUser(HttpServletResponse httpServletResponse) {
        // Invalidate the refresh token cookie
        Cookie refreshTokenCookie = new Cookie("refreshToken", null);
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setPath("/");
        // refreshTokenCookie.setSecure(true);
        refreshTokenCookie.setMaxAge(0); // Delete the cookie
        httpServletResponse.addCookie(refreshTokenCookie);
    }

    // REFRESH ACCESS TOKEN
    public UserAuthResponseDTO refreshAccessToken(String refreshToken, HttpServletResponse httpServletResponse) {
        // Validate Refresh Token
        String userEmail = jwtService.extractEmail(refreshToken);
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User with email " + userEmail + " not found"));

        CustomUserDetails userDetails = new CustomUserDetails(user);
        if (!jwtService.isTokenValid(refreshToken, userDetails)) {
            throw new RuntimeException("Invalid Refresh Token");
        }

        // Generate new tokens and return response
        String newAccessToken = jwtService.generateAccessToken(user.getEmail(), user.getRole().name());

        return new UserAuthResponseDTO(
                user.getEmail(),
                newAccessToken,
                user.getRole().name(),
                TimeUnit.MINUTES.toMillis(30) // 30 minutes
        );
    }

    // Auth response helper method
    private UserAuthResponseDTO authResponse(User savedUser, HttpServletResponse httpServletResponse) {
        CustomUserDetails customUserDetails = new CustomUserDetails(savedUser);

        String accessToken = jwtService.generateAccessToken(savedUser.getEmail(), savedUser.getRole().name());
        String refreshToken = jwtService.generateRefreshToken(savedUser.getEmail());

        // Set Refresh Token as a HttpOnly cookie
        Cookie refreshTokenCookie = new Cookie("refreshToken", refreshToken);
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setPath("/");
        // refreshTokenCookie.setSecure(true);
        refreshTokenCookie.setMaxAge((int) TimeUnit.DAYS.toSeconds(30)); // 30 days
        httpServletResponse.addCookie(refreshTokenCookie);

        return new UserAuthResponseDTO(
                savedUser.getEmail(),
                accessToken,
                savedUser.getRole().name(),
                TimeUnit.MINUTES.toMillis(30) // 30 minutes
        );
    }

}
