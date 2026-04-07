package com.faculty.ems.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    @GetMapping("/dashboard")
    public String showDashboard(Authentication auth, Model model) {
        // This must match the folder and file name: templates/dashboard/dashboard.html
        model.addAttribute("username",auth.getName());

        String role = auth.getAuthorities().iterator().next().getAuthority();

        model.addAttribute("role",role);

        return "dashboard/dashboard";
    }

}
