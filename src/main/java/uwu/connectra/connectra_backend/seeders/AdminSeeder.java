package uwu.connectra.connectra_backend.seeders;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import uwu.connectra.connectra_backend.entities.Admin;
import uwu.connectra.connectra_backend.entities.Role;
import uwu.connectra.connectra_backend.repositories.AdminRepository;

// Create a default admin account in the database in the first time database start
@Slf4j
@Component
@RequiredArgsConstructor
public class AdminSeeder implements CommandLineRunner {

    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${ADMIN_EMAIL}")
    private String admin_email;

    @Value("${ADMIN_PASSWORD}")
    private String admin_row_password;

    @Override
    public void run(String... args) {
        if (!adminRepository.findByEmail(admin_email)) {
            String hashed_password = passwordEncoder.encode(admin_row_password);

            Admin adminUser = new Admin();
            adminUser.setFirst_name("Connectra");
            adminUser.setLast_name("Admin");
            adminUser.setEmail(admin_email);
            adminUser.setHashed_password(hashed_password);
            adminUser.setRole(Role.ADMIN);

            adminRepository.save(adminUser);
            log.info("✅ Default Admin account created!");
        } else {
            log.info("💻 Admin account already exists. Skipping seeding.");
        }
    }
}