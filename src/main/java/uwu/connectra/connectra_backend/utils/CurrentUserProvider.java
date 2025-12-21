package uwu.connectra.connectra_backend.utils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import uwu.connectra.connectra_backend.entities.Role;
import uwu.connectra.connectra_backend.entities.User;
import uwu.connectra.connectra_backend.exceptions.UserNotFoundException;
import uwu.connectra.connectra_backend.repositories.UserRepository;


/**
 * Utility component that provides access to information about the
 * currently authenticated user in the system.
 *
 * <p>This class acts as a centralized helper for retrieving user-related
 * data from the Spring Security context.</p>
 *
 * <p>Available methods:</p>
 * <ul>
 *     <li>{@link #getCurrentUserEmail()}</li>
 *     <li>{@link #getCurrentUser()}</li>
 *     <li>{@link #getCurrentUserRole()}</li>
 *     <li>{@link #getCurrentUserAs(Class)}</li>
 * </ul>
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CurrentUserProvider {

    private final UserRepository userRepository;

    // Get the email of the currently authenticated user
    public String getCurrentUserEmail() {
        Authentication authentication = getAuthentication();
        String email = authentication.getName();
        log.debug("Retrieved current user email: {}", email);
        return email;
    }

    // Get the currently authenticated User entity from the database
    public User getCurrentUser() {
        String email = getCurrentUserEmail();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.error("Authenticated user not found in database: {}", email);
                    return new UserNotFoundException("User not found: " + email);
                });
        log.debug("Retrieved current user: {} (ID: {}, Role: {})", email, user.getId(), user.getRole());
        return user;
    }

    // Get the role of the currently authenticated user
    public Role getCurrentUserRole() {
        return getCurrentUser().getRole();
    }

    // Return the currently authenticated user but as the specified subclass type
    public <T extends User> T getCurrentUserAs(Class<T> type) {
        User user = getCurrentUser();

        if (!type.isInstance(user)) {
            log.error("Type mismatch: Expected {}, but found {} for user {}",
                    type.getSimpleName(), user.getClass().getSimpleName(), user.getEmail());
            throw new ClassCastException(
                    String.format("Current user is not of type %s. User role: %s",
                            type.getSimpleName(), user.getRole()));
        }

        log.debug("Successfully cast user {} to type {}", user.getEmail(), type.getSimpleName());
        return type.cast(user);
    }

    // Helper method to get the current Authentication object
    private Authentication getAuthentication() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            log.error("No authenticated user found in SecurityContext");
            throw new IllegalStateException("No authenticated user found");
        }

        return authentication;
    }
}
