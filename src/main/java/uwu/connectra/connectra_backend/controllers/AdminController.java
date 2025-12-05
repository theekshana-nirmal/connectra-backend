package uwu.connectra.connectra_backend.controllers;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/dashboard")
    public String adminDashboard() {
        return "Welcome to the Admin Dashboard!";
    }
}
