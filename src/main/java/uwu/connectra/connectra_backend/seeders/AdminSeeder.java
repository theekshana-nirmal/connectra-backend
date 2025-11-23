package uwu.connectra.connectra_backend.seeders;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import uwu.connectra.connectra_backend.entities.Admin;
import uwu.connectra.connectra_backend.repositories.AdminRepository;

// This class use to create a default admin account in the database in the first time database start
@Component
@RequiredArgsConstructor
public class AdminSeeder implements CommandLineRunner {

    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${ADMIN_EMAIL}")
    private String admin_email;

    @Value("${ADMIN_PASSWORD}")
    private String admin_row_password;

    String hashed_password = passwordEncoder.encode(admin_row_password);

    @Override
    public void run(String... args){
        if(!adminRepository.existsByEmail("${ADMIN_EMAIL}")){
            Admin adminUser = new Admin();
            adminUser.setFirst_name("Connectra");
            adminUser.setLast_name("Admin");
            adminUser.setEmail(admin_email);
            adminUser.setHashed_password(hashed_password);

            adminRepository.save(adminUser);
            System.out.println("Default Admin account created!");
        }
    }
}