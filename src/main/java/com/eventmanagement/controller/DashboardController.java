package com.eventmanagement.controller;

import com.eventmanagement.model.Role;
import com.eventmanagement.model.User;
import com.eventmanagement.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/")
    public String home(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            return "redirect:/dashboard";
        }
        return "redirect:/auth/login";
    }

    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication) {
        if (authentication == null) {
            return "redirect:/auth/login";
        }
        User user = userRepository.findByEmail(authentication.getName()).orElse(null);
        if (user != null && user.getRole() == Role.ADMIN) {
            return "redirect:/admin/dashboard";
        }
        return "redirect:/events/my";
    }
}
