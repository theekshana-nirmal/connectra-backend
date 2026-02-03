package uwu.connectra.connectra_backend.services;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uwu.connectra.connectra_backend.dtos.auth.RegisterResponseDTO;
import uwu.connectra.connectra_backend.dtos.auth.UserAuthResponseDTO;
import uwu.connectra.connectra_backend.dtos.auth.UserLoginRequestDTO;
import uwu.connectra.connectra_backend.dtos.auth.UserRegisterRequestDTO;
import uwu.connectra.connectra_backend.dtos.lecturer.LecturerResponseDTO;
import uwu.connectra.connectra_backend.entities.*;
import uwu.connectra.connectra_backend.exceptions.*;
import uwu.connectra.connectra_backend.repositories.UserRepository;
import uwu.connectra.connectra_backend.utils.StudentDetailsExtractor;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final StudentDetailsExtractor studentDetailsExtractor;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final OtpService otpService;
    private final EmailService emailService;

    /**
     * Initiates student registration by sending OTP to email.
     * User is NOT saved to database until OTP is verified.
     */
    @Transactional
    public RegisterResponseDTO registerUser(UserRegisterRequestDTO request) {
        log.info("Attempting to register user with email: {}", request.getEmail());
        String email = request.getEmail().trim();

        // Check if user already exists in DB
        if (userRepository.findByEmail(email).isPresent()) {
            log.warn("Registration failed: User with email {} already exists", email);
            throw new UserAlreadyExistsException("User with email " + email + " already exists");
        }

        // Validate role - only students can self-register
        Role role;
        try {
            role = Role.valueOf(request.getRole().toUpperCase());
        } catch (IllegalArgumentException e) {
            log.error("Invalid role provided: {}", request.getRole());
            throw new InvalidRoleException("Invalid role: " + request.getRole());
        }

        if (role != Role.STUDENT) {
            log.warn("Attempt to register non-STUDENT role blocked for email: {}", email);
            throw new InvalidRoleException("Only students can register. Contact admin for other roles.");
        }

        // Hash the password for storage
        String hashedPassword = passwordEncoder.encode(request.getPassword().trim());

        // Create verification token with registration data (user NOT saved yet)
        String otp = otpService.createVerificationToken(
                email,
                request.getFirstName().trim(),
                request.getLastName().trim(),
                hashedPassword);

        // Send OTP email
        emailService.sendOtpEmail(email, otp, request.getFirstName().trim());

        log.info("OTP sent to email: {}", email);

        return new RegisterResponseDTO(
                email,
                "Please check your email for the verification code.",
                true);
    }

    /**
     * Verifies the email OTP and creates the user account.
     * User is only saved to DB after successful OTP verification.
     */
    @Transactional
    public UserAuthResponseDTO verifyEmailAndActivateUser(String email, String otp,
            HttpServletResponse httpServletResponse) {
        log.info("Attempting to verify email for: {}", email);

        // Check if user already exists (e.g., already verified)
        if (userRepository.findByEmail(email).isPresent()) {
            log.warn("User already exists for email: {}", email);
            throw new BadRequestException("Account already verified. Please login.");
        }

        // Verify OTP and get the pending registration data
        EmailVerificationToken token = otpService.verifyOtpAndGetToken(email, otp);

        // Extract student details from email
        String studentRegistrationId = studentDetailsExtractor.extractStudentId(email);
        String degree = studentDetailsExtractor.extractDegree(email);
        int batch = studentDetailsExtractor.extractBatch(email);

        // Create the student
        Student student = new Student();
        student.setStudentId(studentRegistrationId);
        student.setDegree(degree);
        student.setBatch(batch);
        student.setRole(Role.STUDENT);
        student.setFirstName(token.getFirstName());
        student.setLastName(token.getLastName());
        student.setEmail(email);
        student.setHashedPassword(token.getHashedPassword());
        student.setEmailVerified(true);
        student.setAccountStatus(AccountStatus.ACTIVE);

        // Save user to database
        User savedUser = userRepository.save(student);
        log.info("User created and activated: {}", savedUser.getEmail());

        // Cleanup OTP tokens
        otpService.cleanupTokens(email);

        return authResponse(savedUser, httpServletResponse);
    }

    /**
     * Resends the OTP to the user's email.
     */
    @Transactional
    public void resendOtp(String email) {
        log.info("Attempting to resend OTP for: {}", email);

        // Check if user already exists (already verified)
        if (userRepository.findByEmail(email).isPresent()) {
            log.warn("User already verified, cannot resend OTP for: {}", email);
            throw new BadRequestException("Account already verified. Please login.");
        }

        // Check if there's a pending registration
        if (!otpService.hasPendingRegistration(email)) {
            log.warn("No pending registration for: {}", email);
            throw new BadRequestException("No pending registration found. Please register first.");
        }

        // Get the existing token to retrieve registration data
        // Since we can't get the data without verifying, we need to ask user to
        // register again
        throw new BadRequestException("Please register again to receive a new verification code.");
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
        lecturer.setFirstName(request.getFirstName().trim());
        lecturer.setLastName(request.getLastName().trim());
        lecturer.setEmail(request.getEmail().trim());
        lecturer.setHashedPassword(passwordEncoder.encode(request.getPassword().trim()));
        lecturer.setEmailVerified(true); // Lecturers created by admin are auto-verified

        // Save user to the database
        User savedUser = userRepository.save(lecturer);
        log.info("Lecturer created successfully: {}", savedUser.getEmail());

        return new LecturerResponseDTO(
                savedUser.getId(),
                savedUser.getFirstName(),
                savedUser.getLastName(),
                savedUser.getEmail(),
                savedUser.getAccountStatus().name());
    }

    // USER LOGIN
    public UserAuthResponseDTO loginUser(UserLoginRequestDTO request, HttpServletResponse httpServletResponse)
            throws UserCredentialsInvalidException {
        log.info("Attempting login for user: {}", request.getEmail());

        // Check if user exists
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> {
                    log.warn("Login failed: User not found: {}", request.getEmail());
                    return new UserCredentialsInvalidException("Your email or password is incorrect");
                });

        // Check if email is verified (for students)
        if (!user.isEmailVerified() && user.getRole() == Role.STUDENT) {
            log.warn("Login blocked: Email not verified for: {}", request.getEmail());
            throw new BadRequestException(
                    "Please verify your email before logging in. Check your inbox for the verification code.");
        }

        // Check if account is deactivated
        if (user.getAccountStatus() == AccountStatus.DEACTIVATED) {
            log.warn("Login blocked: Account deactivated for: {}", request.getEmail());
            throw new BadRequestException(
                    "Your account has been deactivated. Please contact an administrator.");
        }

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()));
        } catch (Exception e) {
            log.warn("Login failed for user: {}. Invalid credentials.", request.getEmail());
            throw new UserCredentialsInvalidException("Your email or password is incorrect");
        }

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
