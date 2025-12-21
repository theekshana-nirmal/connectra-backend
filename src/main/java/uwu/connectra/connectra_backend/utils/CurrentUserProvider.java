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

@Component
@RequiredArgsConstructor
@Slf4j
public class CurrentUserProvider {

    private final UserRepository userRepository;

    public String getCurrentUserEmail() {
        Authentication authentication = getAuthentication();
        String email = authentication.getName();
        log.debug("Retrieved current user email: {}", email);
        return email;
    }

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

    public Role getCurrentUserRole() {
        return getCurrentUser().getRole();
    }

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

    private Authentication getAuthentication() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            log.error("No authenticated user found in SecurityContext");
            throw new IllegalStateException("No authenticated user found");
        }

        return authentication;
    }
}
