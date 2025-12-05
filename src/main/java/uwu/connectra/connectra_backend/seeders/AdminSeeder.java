package uwu.connectra.connectra_backend.seeders;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import uwu.connectra.connectra_backend.entities.Admin;
import uwu.connectra.connectra_backend.entities.Role;
import uwu.connectra.connectra_backend.repositories.UserRepository;

// Create a default admin account in the database in the first time database start
@Slf4j
@Component
@RequiredArgsConstructor
public class AdminSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${ADMIN_EMAIL}")
    private String admin_email;

    @Value("${ADMIN_PASSWORD}")
    private String admin_row_password;

    @Override
    public void run(String... args) {
        if (!userRepository.existsByEmail(admin_email)) {
            String hashed_password = passwordEncoder.encode(admin_row_password);

            Admin adminUser = new Admin();
            adminUser.setFirstName("Connectra");
            adminUser.setLastName("Admin");
            adminUser.setEmail(admin_email);
            adminUser.setHashedPassword(hashed_password);
            adminUser.setRole(Role.ADMIN);

            userRepository.save(adminUser);
            log.info("✅ Default Admin account created!");
        } else {
            log.info("💻 Admin account already exists. Skipping seeding.");
        }
    }
}