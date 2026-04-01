package com.eventmanagement.controller;

import com.eventmanagement.model.Event;
import com.eventmanagement.model.EventStatus;
import com.eventmanagement.model.User;
import com.eventmanagement.repository.UserRepository;
import com.eventmanagement.service.EventService;
import com.eventmanagement.service.VenueService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Controller
@RequestMapping("/events")
public class EventController {

    @Autowired
    private EventService eventService;

    @Autowired
    private VenueService venueService;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/my")
    public String myEvents(Authentication authentication, Model model) {
        User user = userRepository.findByEmail(authentication.getName()).orElseThrow();
        model.addAttribute("events", eventService.getEventsByOrganizer(user));
        model.addAttribute("user", user);
        return "events/my-events";
    }

    @GetMapping("/new")
    public String newEventForm(Model model) {
        model.addAttribute("event", new Event());
        model.addAttribute("venues", venueService.getAvailableVenues());
        return "events/create";
    }

    @PostMapping("/new")
    public String createEvent(@ModelAttribute Event event,
                              @RequestParam Long venueId,
                              @RequestParam String startDateTimeStr,
                              @RequestParam String endDateTimeStr,
                              Authentication authentication,
                              RedirectAttributes redirectAttributes) {
        try {
            User user = userRepository.findByEmail(authentication.getName()).orElseThrow();
            event.setOrganizer(user);
            event.setVenue(venueService.getVenueById(venueId).orElseThrow());
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
            event.setStartDateTime(LocalDateTime.parse(startDateTimeStr, formatter));
            event.setEndDateTime(LocalDateTime.parse(endDateTimeStr, formatter));

            if (event.getEndDateTime().isBefore(event.getStartDateTime()) ||
                event.getEndDateTime().isEqual(event.getStartDateTime())) {
                redirectAttributes.addFlashAttribute("error", "End time must be after start time.");
                return "redirect:/events/new";
            }

            Event saved = eventService.createEvent(event);
            if (saved.getStatus() == EventStatus.CONFLICT) {
                redirectAttributes.addFlashAttribute("warning",
                    "Booking submitted but CONFLICT detected: " + saved.getConflictNote());
            } else {
                redirectAttributes.addFlashAttribute("success", "Event booking submitted and is pending approval.");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to create event: " + e.getMessage());
        }
        return "redirect:/events/my";
    }

    @GetMapping("/{id}")
    public String eventDetail(@PathVariable Long id, Model model, Authentication authentication) {
        Event event = eventService.getEventById(id)
            .orElseThrow(() -> new IllegalArgumentException("Event not found"));
        User currentUser = userRepository.findByEmail(authentication.getName()).orElseThrow();
        boolean isAdmin = currentUser.getRole().name().equals("ADMIN");
        boolean isOwner = event.getOrganizer().getId().equals(currentUser.getId());
        if (!isAdmin && !isOwner) {
            return "redirect:/events/my";
        }
        model.addAttribute("event", event);
        return "events/detail";
    }

    @PostMapping("/{id}/delete")
    public String deleteEvent(@PathVariable Long id, Authentication authentication,
                              RedirectAttributes redirectAttributes) {
        Event event = eventService.getEventById(id)
            .orElseThrow(() -> new IllegalArgumentException("Event not found"));
        User currentUser = userRepository.findByEmail(authentication.getName()).orElseThrow();
        boolean isAdmin = currentUser.getRole().name().equals("ADMIN");
        boolean isOwner = event.getOrganizer().getId().equals(currentUser.getId());
        if (isAdmin || (isOwner && event.getStatus() == EventStatus.PENDING)) {
            eventService.deleteEvent(id);
            redirectAttributes.addFlashAttribute("success", "Event deleted.");
        } else {
            redirectAttributes.addFlashAttribute("error", "You cannot delete this event.");
        }
        return "redirect:/events/my";
    }

    @GetMapping("/venues")
    public String allVenues(Model model) {
        model.addAttribute("venues", venueService.getAllVenues());
        return "venues/list";
    }
}
