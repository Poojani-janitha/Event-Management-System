package com.faculty.ems.controller;
import com.faculty.ems.model.Venue;
import com.faculty.ems.service.VenueService;
import com.faculty.ems.service.CalendarService;
import com.faculty.ems.dto.CalendarDto;
import com.faculty.ems.dto.VenueFilterCriteria;
import com.faculty.ems.dto.BookingFilterCriteria;
import com.faculty.ems.service.VenueFilterService;
import com.faculty.ems.service.BookingFilterService;
import com.faculty.ems.service.SocietyService;
import com.faculty.ems.repository.VenueBookingRepository;
import com.faculty.ems.service.VenueBookingService;
import com.faculty.ems.model.VenueBooking;
import com.faculty.ems.model.VenueBooking.BookingStatus;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.time.LocalDate;
import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;

@Controller
@RequestMapping("/venues")
public class VenueController {
    @Autowired
    private VenueService venueService;

    @Autowired
    private CalendarService calendarService;

    @Autowired
    private VenueFilterService venueFilterService;

    @Autowired
    private VenueBookingRepository bookingRepo;

    @Autowired
    private SocietyService societyService;

    @Autowired
    private BookingFilterService bookingFilterService;
    
    @Autowired
    private VenueBookingService venueBookingService;

    @GetMapping
    public String listVenues(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) String searchTerm,
            @RequestParam(required = false) Integer minCapacity,
            @RequestParam(required = false) Boolean activeStatus,
            Model model) {
        
        // 1. Fetch all venues
        List<Venue> venues = venueService.getAllVenues();

        // 2. Build filter criteria from request parameters
        VenueFilterCriteria criteria = VenueFilterCriteria.builder()
                .searchTerm(searchTerm)
                .minCapacity(minCapacity)
                .active(activeStatus)
                .build();

        // 3. Apply filtering using Strategy Pattern
        List<Venue> filteredVenues = venueFilterService.filter(venues, criteria);
        
        // 4. Add data to model
        model.addAttribute("venues", filteredVenues);
        model.addAttribute("filterCriteria", criteria);
        
        LocalDate today = LocalDate.now();
        if (year == null) year = today.getYear();
        if (month == null) month = today.getMonthValue();
        
        // Fetch global bookings
        CalendarDto calendarDto = calendarService.getGlobalCalendarData(year, month);
        
        // Pre-calculate positions for Thymeleaf to use
        List<Double> headerPositions = Arrays.asList(0.0, 75.0, 150.0, 225.0, 300.0, 375.0, 450.0);
        int startDayOfWeek = calendarDto.getStartDayOfWeek();
        int offset = (startDayOfWeek == 7) ? 0 : startDayOfWeek;
        
        List<Double> cellLefts = new ArrayList<>();
        List<Double> cellTops = new ArrayList<>();
        
        double cellWidth = 75.0;
        double cellHeight = 75.0;
        double startX = 0;
        double startY = 50.0;
        
        for (int i = 0; i < 42; i++) {
            int row = i / 7;
            int col = i % 7;
            cellLefts.add(startX + (col * cellWidth));
            cellTops.add(startY + (row * cellHeight));
        }
        
        model.addAttribute("calendar", calendarDto);
        model.addAttribute("currentYear", year);
        model.addAttribute("currentMonth", month);
        model.addAttribute("offset", offset);
        model.addAttribute("headerPositions", headerPositions);
        model.addAttribute("cellLefts", cellLefts);
        model.addAttribute("cellTops", cellTops);
        
        return "venues/list";
    }

    @GetMapping("/addVenue")
    public String showCreateForm(Model model) {
        model.addAttribute("venue", new Venue());
        return "venues/addVenue";
    }

    @PostMapping("/save")
    public String saveNewVenue(@Valid @ModelAttribute("venue") Venue venue,
                               BindingResult result,
                               RedirectAttributes ra,
                               Model model) {
        if (result.hasErrors()) {
            String errorMessage = result.getAllErrors().get(0).getDefaultMessage();
            model.addAttribute("message", errorMessage);
            model.addAttribute("messageType", "danger");
            model.addAttribute("status", "error");
            model.addAttribute("venue", venue);
            return "venues/addVenue";
        }
        
        try {
            venue.setActive(true);
            venueService.saveVenue(venue);
            ra.addFlashAttribute("message", "Venue added successfully!");
            ra.addFlashAttribute("messageType", "success");
            ra.addFlashAttribute("status", "success");
            return "redirect:/venues";
        } catch (IllegalArgumentException e) {
            model.addAttribute("message", e.getMessage());
            model.addAttribute("messageType", "danger");
            model.addAttribute("status", "error");
            model.addAttribute("venue", venue);
            return "venues/addVenue";
        } catch (Exception e) {
            model.addAttribute("message", "Error saving venue: " + e.getMessage());
            model.addAttribute("messageType", "danger");
            model.addAttribute("status", "error");
            model.addAttribute("venue", venue);
            return "venues/addVenue";
        }
    }

    @GetMapping("/{id}")
    public String viewVenue(@PathVariable Long id, Model model) {
        Venue venue = venueService.getVenueById(id);
        if (venue == null) {
            return "redirect:/venues";
        }
        model.addAttribute("venue", venue);
        return "venues/view";
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model) {
        Venue venue = venueService.getVenueById(id);
        if (venue == null) {
            return "redirect:/venues";
        }
        model.addAttribute("venue", venue);
        return "venues/editVenue";
    }

    @PostMapping("/{id}/update")
    public String updateVenue(@PathVariable Long id,
                              @Valid @ModelAttribute("venue") Venue venue,
                              BindingResult result,
                              RedirectAttributes ra) {
        if (result.hasErrors()) {
            String errorMessage = result.getAllErrors().get(0).getDefaultMessage();
            ra.addFlashAttribute("message", errorMessage);
            ra.addFlashAttribute("messageType", "danger");
            ra.addFlashAttribute("status", "error");
            return "redirect:/venues/" + id + "/edit";
        }
        
        Venue existingVenue = venueService.getVenueById(id);
        if (existingVenue == null) {
            return "redirect:/venues";
        }
        
        try {
            existingVenue.setName(venue.getName());
            existingVenue.setLocation(venue.getLocation());
            existingVenue.setCapacity(venue.getCapacity());
            existingVenue.setDescription(venue.getDescription());
            existingVenue.setHas_ac(venue.getHas_ac());
            existingVenue.setHas_projector(venue.getHas_projector());
            existingVenue.setHas_sound(venue.getHas_sound());
            existingVenue.setActive(venue.getActive());
            
            venueService.saveVenue(existingVenue);
            ra.addFlashAttribute("message", "Venue updated successfully!");
            return "redirect:/venues/" + id;
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("message", e.getMessage());
            ra.addFlashAttribute("messageType", "danger");
            ra.addFlashAttribute("status", "error");
            return "redirect:/venues/" + id + "/edit";
        } catch (Exception e) {
            ra.addFlashAttribute("message", "Error updating venue: " + e.getMessage());
            ra.addFlashAttribute("messageType", "danger");
            ra.addFlashAttribute("status", "error");
            return "redirect:/venues/" + id + "/edit";
        }
    }


    @PostMapping("/{id}/delete")
    public String deleteVenue(@PathVariable Long id, RedirectAttributes ra) {
        try {
            venueService.deleteVenue(id);
            ra.addFlashAttribute("message", "Venue deleted successfully!");
            ra.addFlashAttribute("messageType", "success");
            ra.addFlashAttribute("status", "success");
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("message", e.getMessage());
            ra.addFlashAttribute("messageType", "danger");
            ra.addFlashAttribute("status", "error");
        } catch (Exception e) {
            ra.addFlashAttribute("message", "Error deleting venue: " + e.getMessage());
            ra.addFlashAttribute("messageType", "danger");
            ra.addFlashAttribute("status", "error");
        }
        return "redirect:/venues";
    }

    @GetMapping("/history")
    public String viewBookingHistory(
            @RequestParam(required = false) Long venueId,
            @RequestParam(required = false) Integer societyId,
            @RequestParam(required = false) String status,
            Model model) {
        
        List<VenueBooking> allBookings = bookingRepo.findAll();
        
        // Calculate Statistics
        long total = allBookings.size();
        long approved = allBookings.stream().filter(b -> b.getStatus() == VenueBooking.BookingStatus.APPROVED).count();
        long pending = allBookings.stream().filter(b -> b.getStatus() == VenueBooking.BookingStatus.PENDING).count();
        long rejected = allBookings.stream().filter(b -> b.getStatus() == VenueBooking.BookingStatus.REJECTED).count();
        long cancelled = allBookings.stream().filter(b -> b.getStatus() == VenueBooking.BookingStatus.CANCELLED).count();
        long postponed = allBookings.stream().filter(b -> b.getStatus() == VenueBooking.BookingStatus.POSTPONED).count();
        
        // Apply Filtering via Strategy Pattern
        BookingFilterCriteria criteria = BookingFilterCriteria.builder()
                .venueId(venueId)
                .societyId(societyId)
                .status(status)
                .build();
        
        List<VenueBooking> filteredBookings = bookingFilterService.filter(allBookings, criteria);
        
        model.addAttribute("bookings", filteredBookings);
        model.addAttribute("filterCriteria", criteria);
        model.addAttribute("venues", venueService.getAllVenues());
        model.addAttribute("societies", societyService.getAllSocieties());
        
        // Stats
        model.addAttribute("totalCount", total);
        model.addAttribute("approvedCount", approved);
        model.addAttribute("pendingCount", pending);
        model.addAttribute("rejectedCount", rejected);
        model.addAttribute("cancelledCount", cancelled);
        model.addAttribute("postponedCount", postponed);
        
        return "venues/history";
    }

    @GetMapping("/requests")
    public String viewBookingRequests(Model model) {
        List<VenueBooking> pendingBookings = bookingRepo.findByStatus(BookingStatus.PENDING);
        model.addAttribute("bookings", pendingBookings);
        return "venues/requests";
    }

    @PostMapping("/requests/{id}/approve")
    public String approveBooking(@PathVariable Long id, @RequestParam(required = false) String adminNote, RedirectAttributes ra) {
        try {
            venueBookingService.approveBooking(id, adminNote);
            ra.addFlashAttribute("message", "Booking approved successfully!");
            ra.addFlashAttribute("messageType", "success");
        } catch (Exception e) {
            ra.addFlashAttribute("message", "Error approving booking: " + e.getMessage());
            ra.addFlashAttribute("messageType", "danger");
        }
        return "redirect:/venues/requests";
    }

    @PostMapping("/requests/{id}/reject")
    public String rejectBooking(@PathVariable Long id, @RequestParam(required = false) String adminNote, RedirectAttributes ra) {
        try {
            venueBookingService.rejectBooking(id, adminNote);
            ra.addFlashAttribute("message", "Booking rejected.");
            ra.addFlashAttribute("messageType", "warning");
        } catch (Exception e) {
            ra.addFlashAttribute("message", "Error rejecting booking: " + e.getMessage());
            ra.addFlashAttribute("messageType", "danger");
        }
        return "redirect:/venues/requests";
    }

    @PostMapping("/history/{id}/postpone")
    public String postponeBooking(@PathVariable Long id,
                                  @RequestParam(required = false) String adminNote,
                                  RedirectAttributes ra) {
        try {
            venueBookingService.postponeApprovedBooking(id, adminNote);
            ra.addFlashAttribute("message", "Booking moved to REJECTED and event marked as POSTPONED.");
            ra.addFlashAttribute("messageType", "warning");
        } catch (Exception e) {
            ra.addFlashAttribute("message", "Error postponing booking: " + e.getMessage());
            ra.addFlashAttribute("messageType", "danger");
        }
        return "redirect:/venues/history";
    }

}
