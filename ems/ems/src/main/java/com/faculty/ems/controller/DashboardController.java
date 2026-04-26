package com.faculty.ems.controller;

import com.faculty.ems.dto.CalendarDto;
import com.faculty.ems.model.Society;
import com.faculty.ems.model.User;
import com.faculty.ems.model.VenueBooking;
import com.faculty.ems.repository.SocietyRepository;
import com.faculty.ems.repository.UserRepository;
import com.faculty.ems.service.CalendarService;
import com.faculty.ems.service.DashboardService;
import com.faculty.ems.service.SocietyService;
import com.faculty.ems.service.SocietyAdminRequestService;
import com.faculty.ems.service.VenueBookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

@Controller
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SocietyRepository societyRepository;

    @Autowired
    private VenueBookingService venueBookingService;

    @Autowired
    private SocietyAdminRequestService  societyAdminRequestService;

    @Autowired
    private SocietyService societyService;

    @Autowired
    private CalendarService calendarService;

    @GetMapping("/dashboard")
    public String showDashboard(Authentication auth, Model model,
                                @RequestParam(required = false) Integer month,
                                @RequestParam(required = false) Integer year) {
        model.addAttribute("username", auth.getName());

        String role = auth.getAuthorities().iterator().next().getAuthority();

        if ("ROLE_ADMIN".equals(role)) {
            model.addAttribute("pendingBookings", dashboardService.getPendingBookings());
            model.addAttribute("pendingSocietyRequest", societyAdminRequestService.getPendingRequests());

        } else if ("ROLE_SOCIETY_ADMIN".equals(role)) {
            User user = userRepository.findByUsername(auth.getName()).orElseThrow();
            List<Society> societies = societyRepository.findAllBySocietyAdminId(user.getId());

            if (societies == null || societies.isEmpty()) {
                model.addAttribute("myBookings", Collections.emptyList());
            } else {
                List<VenueBooking> allBookings = societies.stream()
                        .flatMap(s -> venueBookingService.getBookingsBySociety(s.getId().longValue()).stream())
                        .sorted((a, b) -> {
                            int byDate = b.getBookingDate().compareTo(a.getBookingDate());
                            if (byDate != 0) return byDate;
                            return b.getStartTime().compareTo(a.getStartTime());
                        })
                        .toList();

                model.addAttribute("myBookings", allBookings);
            }
        } else if ("ROLE_MEMBER".equals(role)) {
            // member can see all approved bookings and highlight booking relevant to his socities 
            User user = userRepository.findByUsername(auth.getName()).orElseThrow();
            List<Integer> joinedSocietyIds = societyService.getSocietiesByMemberId(user.getId())
                    .stream()
                    .map(Society::getId)
                    .toList();

            model.addAttribute("approvedBookings", dashboardService.getApprovedBookings());
            model.addAttribute("joinedSocietyIds", joinedSocietyIds);

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
