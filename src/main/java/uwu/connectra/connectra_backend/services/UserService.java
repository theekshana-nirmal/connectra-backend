package uwu.connectra.connectra_backend.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uwu.connectra.connectra_backend.exceptions.UserNotFoundException;
import uwu.connectra.connectra_backend.repositories.UserRepository;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    // DELETE USER BY ID
    public void deleteUserById(Long userId) {
        log.info("Attempting to delete user with ID: {}", userId);
        if (!userRepository.existsById(userId)) {
            log.warn("Deletion failed: User with ID {} not found", userId);
            throw new UserNotFoundException("User with ID " + userId + " not found");
        }
        userRepository.deleteById(userId);
        log.info("User with ID {} deleted successfully", userId);
    }
}
