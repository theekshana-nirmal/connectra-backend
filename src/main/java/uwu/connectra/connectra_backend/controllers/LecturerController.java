package uwu.connectra.connectra_backend.controllers;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/lecturer")
public class LecturerController {
    @PreAuthorize("hasAnyRole('LECTURER', 'ADMIN')")
    @GetMapping("/dashboard")
    public String studentDashboard() {
        return "Welcome to the Lecturer Dashboard!";
    }
}
