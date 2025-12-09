package uwu.connectra.connectra_backend.services;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import uwu.connectra.connectra_backend.dtos.*;
import uwu.connectra.connectra_backend.entities.*;
import uwu.connectra.connectra_backend.exceptions.*;
import uwu.connectra.connectra_backend.repositories.UserRepository;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final StudentDetailsExtractorService studentDetailsExtractorService;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    // USER REGISTRATION
    public UserAuthResponseDTO registerUser(UserRegisterRequestDTO request, HttpServletResponse httpServletResponse) {
        log.info("Attempting to register user with email: {}", request.getEmail());

        // Check if user already exists
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            log.warn("Registration failed: User with email {} already exists", request.getEmail());
            throw new UserAlreadyExistsException("User with email " + request.getEmail() + " already exists");
        }

        User user;

        Role role;
        try {
            role = Role.valueOf(request.getRole().toUpperCase());
        } catch (IllegalArgumentException e) {
            log.error("Invalid role provided: {}", request.getRole());
            throw new InvalidRoleException("Invalid role: " + request.getRole());
        }

        switch (role) {
            case STUDENT -> {
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
            case LECTURER -> {
                log.warn("Attempt to register LECTURER role blocked for email: {}", request.getEmail());
                throw new InvalidRoleException("Cannot register user with role LECTURER");
            }
            case ADMIN -> {
                log.warn("Attempt to register ADMIN role blocked for email: {}", request.getEmail());
                throw new InvalidRoleException("Cannot register user with role ADMIN");
            }
            default -> {
                log.error("Invalid role provided: {}", request.getRole());
                throw new InvalidRoleException("Invalid role: " + request.getRole());
            }
        }

        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setHashedPassword(passwordEncoder.encode(request.getPassword()));

        // Save user to the database
        User savedUser = userRepository.save(user);
        log.info("User registered successfully: {}", savedUser.getEmail());

        return authResponse(savedUser, httpServletResponse);
    }

    // LECTURER CREATION
    public LecturerResponseDTO createLecturer(UserRegisterRequestDTO request) {
        log.info("Attempting to create lecturer account with email: {}", request.getEmail());

        // Check if user already exists
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            log.warn("Creation failed: User with email {} already exists", request.getEmail());
            throw new UserAlreadyExistsException("User with email " + request.getEmail() + " already exists");
        }

        Lecturer lecturer = new Lecturer();
        lecturer.setRole(Role.LECTURER);
        lecturer.setFirstName(request.getFirstName());
        lecturer.setLastName(request.getLastName());
        lecturer.setEmail(request.getEmail());
        lecturer.setHashedPassword(passwordEncoder.encode(request.getPassword()));

        // Save user to the database
        User savedUser = userRepository.save(lecturer);
        log.info("Lecturer created successfully: {}", savedUser.getEmail());

        return new LecturerResponseDTO(
                savedUser.getId(),
                savedUser.getFirstName(),
                savedUser.getLastName(),
                savedUser.getEmail());
    }

    // USER LOGIN
    public UserAuthResponseDTO loginUser(UserLoginRequestDTO request, HttpServletResponse httpServletResponse)
            throws UserCredentialsInvalidException {
        log.info("Attempting login for user: {}", request.getEmail());
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()));
        } catch (Exception e) {
            log.warn("Login failed for user: {}. Invalid credentials.", request.getEmail());
            throw new UserCredentialsInvalidException("Your email or password is incorrect");
        }

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> {
                    log.error("User not found after authentication: {}", request.getEmail());
                    return new UserNotFoundException("User with email " + request.getEmail() + " not found");
                });

        log.info("User logged in successfully: {}", user.getEmail());
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
        log.info("User logged out successfully");
    }

    // REFRESH ACCESS TOKEN
    public UserAuthResponseDTO refreshAccessToken(String refreshToken) {
        // Validate Refresh Token
        String userEmail = jwtService.extractEmail(refreshToken);
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> {
                    log.warn("User not found during token refresh: {}", userEmail);
                    return new RuntimeException("User with email " + userEmail + " not found");
                });

        CustomUserDetails userDetails = new CustomUserDetails(user);
        if (!jwtService.isTokenValid(refreshToken, userDetails)) {
            log.warn("Invalid refresh token for user: {}", userEmail);
            throw new InvalidTokenException("Refresh token is invalid or expired");
        }

        // Generate new tokens and return response
        String newAccessToken = jwtService.generateAccessToken(user.getEmail(), user.getRole().name());
        log.info("Access token refreshed for user: {}", userEmail);

        return new UserAuthResponseDTO(
                user.getEmail(),
                user.getRole().name(),
                newAccessToken,
                TimeUnit.MINUTES.toMillis(30) // 30 minutes
        );
    }

    // Auth response helper method
    private UserAuthResponseDTO authResponse(User savedUser, HttpServletResponse httpServletResponse) {
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
                savedUser.getRole().name(),
                accessToken,
                TimeUnit.MINUTES.toMillis(30) // 30 minutes
        );
    }

}
