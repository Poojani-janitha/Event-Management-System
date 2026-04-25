package com.faculty.ems.controller;

import com.faculty.ems.model.SocietyAdminRequest;
import com.faculty.ems.model.User;
import com.faculty.ems.repository.UserRepository;
import com.faculty.ems.service.DashboardService;
import com.faculty.ems.service.SocietyAdminRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SocietyAdminRequestService  societyAdminRequestService;

    @GetMapping("/dashboard")
    public String showDashboard(Authentication auth, Model model) {
        model.addAttribute("username", auth.getName());

        String role = auth.getAuthorities().iterator().next().getAuthority();

        if ("ROLE_ADMIN".equals(role)) {
            model.addAttribute("pendingBookings", dashboardService.getPendingBookings());
            model.addAttribute("pendingSocietyRequest", societyAdminRequestService.getPendingRequests());

        } else if ("ROLE_SOCIETY_ADMIN".equals(role)) {
            User user = userRepository.findByUsername(auth.getName()).orElseThrow();
            model.addAttribute("myBookings", dashboardService.getBookingsByUser(user.getId()));
        } else if ("ROLE_MEMBER".equals(role)) {

            model.addAttribute("approvedBookings",dashboardService.getApprovedBookings());

        }

        return "dashboard/dashboard";
    }

}
