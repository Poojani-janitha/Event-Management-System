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
import com.faculty.ems.dto.CalendarDto;
import com.faculty.ems.service.CalendarService;
import org.springframework.web.bind.annotation.RequestParam;
import java.time.LocalDate;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SocietyAdminRequestService societyAdminRequestService;

    @Autowired
    private CalendarService calendarService;

    @GetMapping("/dashboard")
    public String showDashboard(
            Authentication auth,
            Model model,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year) {
        
        model.addAttribute("username", auth.getName());

        String role = auth.getAuthorities().iterator().next().getAuthority();

        if ("ROLE_ADMIN".equals(role)) {
            model.addAttribute("pendingBookings", dashboardService.getPendingBookings());
            model.addAttribute("pendingSocietyRequest", societyAdminRequestService.getPendingRequests());

        } else if ("ROLE_SOCIETY_ADMIN".equals(role)) {
            User user = userRepository.findByUsername(auth.getName()).orElseThrow();
            model.addAttribute("myBookings", dashboardService.getBookingsByUser(user.getId()));
        } else if ("ROLE_MEMBER".equals(role)) {
            model.addAttribute("approvedBookings", dashboardService.getApprovedBookings());
        }

        // Calendar Logic
        int currentMonth = (month != null) ? month : LocalDate.now().getMonthValue();
        int currentYear = (year != null) ? year : LocalDate.now().getYear();

        CalendarDto calendar = calendarService.getGlobalCalendarData(currentYear, currentMonth);
        model.addAttribute("calendar", calendar);
        model.addAttribute("currentMonth", currentMonth);
        model.addAttribute("currentYear", currentYear);
        
        // Needed for the grid calculation in Thymeleaf
        model.addAttribute("offset", calendar.getStartDayOfWeek() == 7 ? 0 : calendar.getStartDayOfWeek()); // Adjusting ISO to Sun-start
        
        // Positional data for CSS grid (7 columns)
        int[] cellLefts = new int[42];
        int[] cellTops = new int[42];
        for (int i = 0; i < 42; i++) {
            cellLefts[i] = (i % 7) * 75;
            cellTops[i] = 50 + (i / 7) * 75;
        }
        model.addAttribute("cellLefts", cellLefts);
        model.addAttribute("cellTops", cellTops);
        
        int[] headerPositions = new int[7];
        for (int i = 0; i < 7; i++) {
            headerPositions[i] = i * 75;
        }
        model.addAttribute("headerPositions", headerPositions);

        return "dashboard/dashboard";
    }

}
