package com.faculty.ems.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    @GetMapping("/dashboard")
    public String showDashboard() {
        // This must match the folder and file name: templates/dashboard/dashboard.html
        return "dashboard/dashboard";
    }

}
