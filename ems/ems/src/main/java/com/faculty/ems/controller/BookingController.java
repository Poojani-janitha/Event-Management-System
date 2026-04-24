package com.faculty.ems.controller;

//import com.faculty.ems.exception.BookingConflictException;
import com.faculty.ems.exception.BookingConflictException;
import com.faculty.ems.model.VenueBooking;
import com.faculty.ems.model.User;
import com.faculty.ems.model.Society;
import com.faculty.ems.repository.VenueRepository;
import com.faculty.ems.repository.UserRepository;
import com.faculty.ems.repository.SocietyRepository;
import com.faculty.ems.service.EventService;
import com.faculty.ems.service.VenueBookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class BookingController {

    private final VenueBookingService bookingService;
    private final EventService eventService;
    private final VenueRepository venueRepo;
    private final SocietyRepository societyRepo;
    private final UserRepository userRepo;


    @GetMapping("/bookings/new")
    public String showForm(@RequestParam(required = false) Long eventId,
                           Model model,
                           @AuthenticationPrincipal UserDetails currentUser) {
        populateBookingFormModel(model, new VenueBooking(), eventId);
        return "bookings/form";
    }

    @PostMapping("/bookings/new")
    public String submitBooking(@ModelAttribute VenueBooking booking,
                                @RequestParam Long venueId,
                                @RequestParam Long eventId,
                                @AuthenticationPrincipal UserDetails currentUser,
                                Model model,
                                RedirectAttributes ra) {
        User user = userRepo.findByUsername(currentUser.getUsername()).orElseThrow();
        Society society = societyRepo.findBySocietyAdminId(user.getId()).orElse(null);
        if (society == null) {
            populateBookingFormModel(model, booking, eventId);
            model.addAttribute("error", "Your account is not linked to any society. Please contact admin.");
            return "bookings/form";
        }

        booking.setVenue(venueRepo.findById(venueId).orElseThrow());
        booking.setEvent(eventService.findById(eventId));
        booking.setSociety(society);
        booking.setRequestedBy(user);

        try {
            bookingService.requestBooking(booking);
            ra.addFlashAttribute("success", "Booking request submitted! Awaiting admin approval.");
            return "redirect:/bookings/my";
        } catch (BookingConflictException e) {
            populateBookingFormModel(model, booking, eventId);
            model.addAttribute("error", e.getMessage());
            return "bookings/form";
        }
    }


    @GetMapping("/bookings/my")
    public String myBookings(Model model,
                             @AuthenticationPrincipal UserDetails currentUser) {
        User user = userRepo.findByUsername(currentUser.getUsername()).orElseThrow();
        Society society = societyRepo.findBySocietyAdminId(user.getId()).orElse(null);
        if (society == null) {
            model.addAttribute("bookings", java.util.Collections.emptyList());
            model.addAttribute("error", "Your account is not linked to any society. Please contact admin.");
            return "bookings/list";
        }

        model.addAttribute("bookings",
            bookingService.getBookingsBySociety(society.getId().longValue()));
        return "bookings/list";
    }

    private void populateBookingFormModel(Model model, VenueBooking booking, Long selectedEventId) {
        model.addAttribute("booking", booking);
        model.addAttribute("venues", venueRepo.findByActiveTrue());
        model.addAttribute("events", eventService.findAll());
        if (selectedEventId != null) {
            model.addAttribute("selectedEventId", selectedEventId);
        }
    }


    @PostMapping("/bookings/{id}/cancel")
    public String cancel(@PathVariable Long id, RedirectAttributes ra) {
        try {
            bookingService.cancelBooking(id);
            ra.addFlashAttribute("success", "Booking cancelled.");
        } catch (RuntimeException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/bookings/my";
    }
}