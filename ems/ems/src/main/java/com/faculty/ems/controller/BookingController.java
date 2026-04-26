package com.faculty.ems.controller;

//import com.faculty.ems.exception.BookingConflictException;
import com.faculty.ems.dto.VenueBookingFormDto;
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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.validation.Valid;
import org.springframework.validation.BindingResult;

import java.util.List;

@Controller
@RequiredArgsConstructor
@PreAuthorize("hasRole('SOCIETY_ADMIN')")
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
        User user = userRepo.findByUsername(currentUser.getUsername()).orElseThrow();
        List<Long> allowedSocietyIds = getAllowedSocietyIds(user);
        List<com.faculty.ems.model.Event> allowedEvents = eventService.findBySocietyIds(allowedSocietyIds)
                .stream()
                .filter(e -> e.getStatus() == com.faculty.ems.model.Event.EventStatus.DRAFT
                        || e.getStatus() == com.faculty.ems.model.Event.EventStatus.POSTPONED)
                .toList();

        if (eventId != null && allowedEvents.stream().noneMatch(e -> e.getId().equals(eventId))) {
            throw new AccessDeniedException("You cannot book venue for this event");
        }

        VenueBookingFormDto bookingForm = new VenueBookingFormDto();
        bookingForm.setEventId(eventId);
        boolean lockEventSelection = eventId != null;
        populateBookingFormModel(model, bookingForm, allowedEvents, lockEventSelection);
        return "bookings/form";
    }

    @PostMapping("/bookings/new")
    public String submitBooking(@Valid @ModelAttribute("booking") VenueBookingFormDto bookingForm,
                                BindingResult result,
                                @RequestParam(name = "eventLocked", defaultValue = "false") boolean eventLocked,
                                @AuthenticationPrincipal UserDetails currentUser,
                                Model model,
                                RedirectAttributes ra) {
        User user = userRepo.findByUsername(currentUser.getUsername()).orElseThrow();

        List<Long> allowedSocietyIds = getAllowedSocietyIds(user);
        List<com.faculty.ems.model.Event> allowedEvents = eventService.findBySocietyIds(allowedSocietyIds)
                .stream()
                .filter(e -> e.getStatus() == com.faculty.ems.model.Event.EventStatus.DRAFT
                        || e.getStatus() == com.faculty.ems.model.Event.EventStatus.POSTPONED)
                .toList();

        if (result.hasErrors()) {
            populateBookingFormModel(model, bookingForm, allowedEvents, eventLocked);
            return "bookings/form";
        }

        com.faculty.ems.model.Event selectedEvent = eventService.findById(bookingForm.getEventId());
        if (allowedEvents.stream().noneMatch(e -> e.getId().equals(selectedEvent.getId()))) {
            throw new AccessDeniedException("You cannot book venue for this event");
        }

        VenueBooking booking = new VenueBooking();
        booking.setVenue(venueRepo.findById(bookingForm.getVenueId()).orElseThrow());
        booking.setEvent(selectedEvent);
        booking.setSociety(selectedEvent.getSociety());
        booking.setRequestedBy(user);
        booking.setBookingDate(bookingForm.getBookingDate());
        booking.setStartTime(bookingForm.getStartTime());
        booking.setEndTime(bookingForm.getEndTime());

        try {
            bookingService.requestBooking(booking);
            ra.addFlashAttribute("success", "Booking request submitted! Awaiting admin approval.");
            return "redirect:/bookings/my";
        } catch (BookingConflictException e) {
            populateBookingFormModel(model, bookingForm, allowedEvents, eventLocked);
            model.addAttribute("error", e.getMessage());
            return "bookings/form";
        }
    }


    @GetMapping("/bookings/my")
    public String myBookings(Model model,
                             @AuthenticationPrincipal UserDetails currentUser) {
        User user = userRepo.findByUsername(currentUser.getUsername()).orElseThrow();
        List<Society> societies = societyRepo.findAllBySocietyAdminId(user.getId());
        if (societies == null || societies.isEmpty()) {
            model.addAttribute("bookings", java.util.Collections.emptyList());
            model.addAttribute("error", "Your account is not linked to any society. Please contact admin.");
            return "bookings/list";
        }

        List<VenueBooking> allBookings = societies.stream()
                .flatMap(s -> bookingService.getBookingsBySociety(s.getId().longValue()).stream())
                .sorted((a, b) -> {
                    int byDate = b.getBookingDate().compareTo(a.getBookingDate());
                    if (byDate != 0) return byDate;
                    return b.getStartTime().compareTo(a.getStartTime());
                })
                .toList();

        model.addAttribute("bookings", allBookings);
        return "bookings/list";
    }

    private void populateBookingFormModel(Model model,
                                          VenueBookingFormDto booking,
                                          List<com.faculty.ems.model.Event> events,
                                          boolean lockEventSelection) {
        model.addAttribute("booking", booking);
        model.addAttribute("venues", venueRepo.findByActiveTrue());
        model.addAttribute("events", events);
        model.addAttribute("eventLocked", lockEventSelection);

        String selectedEventTitle = events.stream()
                .filter(e -> booking.getEventId() != null && e.getId().equals(booking.getEventId()))
                .map(com.faculty.ems.model.Event::getTitle)
                .findFirst()
                .orElse("");
        model.addAttribute("selectedEventTitle", selectedEventTitle);
    }

    private List<Long> getAllowedSocietyIds(User user) {
        List<Society> adminSocieties = societyRepo.findAllBySocietyAdminId(user.getId());
        return adminSocieties.stream().map(s -> s.getId().longValue()).toList();
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