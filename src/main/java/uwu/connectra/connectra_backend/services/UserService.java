package uwu.connectra.connectra_backend.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import uwu.connectra.connectra_backend.dtos.lecturer.LecturerResponseDTO;
import uwu.connectra.connectra_backend.dtos.lecturer.LecturerUpdateRequestDTO;
import uwu.connectra.connectra_backend.entities.AccountStatus;
import uwu.connectra.connectra_backend.entities.Lecturer;
import uwu.connectra.connectra_backend.entities.Role;
import uwu.connectra.connectra_backend.entities.User;
import uwu.connectra.connectra_backend.exceptions.UserNotFoundException;
import uwu.connectra.connectra_backend.repositories.UserRepository;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // GET USER BY ID
    public LecturerResponseDTO getUserById(Long userId) {
        log.info("Fetching user with ID: {}", userId);
        var user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("User with ID {} not found", userId);
                    return new UserNotFoundException("User with ID " + userId + " not found");
                });
        log.info("User with ID {} found: {}", userId, user.getEmail());
        return new LecturerResponseDTO(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getAccountStatus().name());
    }

    // DELETE USER BY ID (Permanent)
    public void deleteUserById(Long userId) {
        log.info("Attempting to delete user with ID: {}", userId);
        if (!userRepository.existsById(userId)) {
            log.warn("Deletion failed: User with ID {} not found", userId);
            throw new UserNotFoundException("User with ID " + userId + " not found");
        }
        userRepository.deleteById(userId);
        log.info("User with ID {} deleted successfully", userId);
    }

    // ACTIVATE USER ACCOUNT
    public void activateUser(Long userId) {
        log.info("Attempting to activate user with ID: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("Activation failed: User with ID {} not found", userId);
                    return new UserNotFoundException("User with ID " + userId + " not found");
                });
        user.setAccountStatus(AccountStatus.ACTIVE);
        userRepository.save(user);
        log.info("User with ID {} activated successfully", userId);
    }

    // DEACTIVATE USER ACCOUNT (Soft Delete)
    public void deactivateUser(Long userId) {
        log.info("Attempting to deactivate user with ID: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("Deactivation failed: User with ID {} not found", userId);
                    return new UserNotFoundException("User with ID " + userId + " not found");
                });
        user.setAccountStatus(AccountStatus.DEACTIVATED);
        userRepository.save(user);
        log.info("User with ID {} deactivated successfully", userId);
    }

    // === LECTURER RELATED OPERATIONS ===
    // Return All Lecturers
    public List<LecturerResponseDTO> getAllLecturers() {
        log.info("Fetching all lecturers from the database");
        var lecturers = userRepository.findAllByRole(Role.LECTURER);
        return lecturers.stream()
                .map(lecturer -> new LecturerResponseDTO(
                        lecturer.getId(),
                        lecturer.getFirstName(),
                        lecturer.getLastName(),
                        lecturer.getEmail(),
                        lecturer.getAccountStatus().name()))
                .toList();
    }

    // Update a Lecturer Account by ID
    public LecturerResponseDTO updateLecturer(Long lecturerId, LecturerUpdateRequestDTO request) {
        log.info("Updating lecturer with ID: {}", lecturerId);
        Lecturer user = (Lecturer) userRepository.findById(lecturerId)
                .orElseThrow(() -> {
                    log.warn("Update failed: Lecturer with ID {} not found", lecturerId);
                    return new UserNotFoundException("Lecturer with ID " + lecturerId + " not found");
                });

        user.setFirstName(request.getFirstName().trim());
        user.setLastName(request.getLastName().trim());

        // Update email only if it's not already taken by another user
        String newEmail = request.getEmail().trim();
        if (userRepository.existsByEmailAndIdNot(newEmail, lecturerId)) {
            log.warn("Update failed: Email {} is already in use by another account", newEmail);
            throw new IllegalArgumentException("Email " + newEmail + " is already in use by another account");
        }
        user.setEmail(newEmail);

        // Update password only if provided
        if (request.getPassword() != null && !request.getPassword().trim().isEmpty()) {
            log.info("Updating password for lecturer with ID: {}", lecturerId);
            user.setHashedPassword(passwordEncoder.encode(request.getPassword().trim()));
        }

        userRepository.save(user);
        log.info("Lecturer with ID {} updated successfully", lecturerId);

        return new LecturerResponseDTO(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getAccountStatus().name());
    }
}
