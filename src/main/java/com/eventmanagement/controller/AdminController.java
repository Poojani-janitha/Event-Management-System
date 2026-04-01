package com.eventmanagement.controller;

import com.eventmanagement.model.Event;
import com.eventmanagement.model.User;
import com.eventmanagement.model.Venue;
import com.eventmanagement.model.VenueType;
import com.eventmanagement.service.EventService;
import com.eventmanagement.service.UserService;
import com.eventmanagement.service.VenueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    @Autowired
    private EventService eventService;

    @Autowired
    private VenueService venueService;

    @Autowired
    private UserService userService;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        List<Event> allEvents = eventService.getAllEvents();
        List<Event> pendingEvents = eventService.getPendingEvents();
        List<Event> conflictEvents = eventService.getConflictEvents();
        List<Venue> venues = venueService.getAllVenues();
        List<User> users = userService.getAllUsers();

        model.addAttribute("totalEvents", allEvents.size());
        model.addAttribute("pendingEvents", pendingEvents.size());
        model.addAttribute("conflictEvents", conflictEvents.size());
        model.addAttribute("totalVenues", venues.size());
        model.addAttribute("totalUsers", users.size());
        model.addAttribute("recentEvents", allEvents.stream().limit(5).toList());
        return "admin/dashboard";
    }

    @GetMapping("/events")
    public String manageEvents(Model model) {
        model.addAttribute("events", eventService.getAllEvents());
        model.addAttribute("pendingEvents", eventService.getPendingEvents());
        model.addAttribute("conflictEvents", eventService.getConflictEvents());
        return "admin/events";
    }

    @PostMapping("/events/{id}/approve")
    public String approveEvent(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            eventService.approveEvent(id);
            redirectAttributes.addFlashAttribute("success", "Event approved successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to approve event: " + e.getMessage());
        }
        return "redirect:/admin/events";
    }

    @PostMapping("/events/{id}/reject")
    public String rejectEvent(@PathVariable Long id,
                              @RequestParam(required = false) String reason,
                              RedirectAttributes redirectAttributes) {
        try {
            eventService.rejectEvent(id, reason);
            redirectAttributes.addFlashAttribute("success", "Event rejected.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to reject event: " + e.getMessage());
        }
        return "redirect:/admin/events";
    }

    @PostMapping("/events/{id}/delete")
    public String deleteEvent(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            eventService.deleteEvent(id);
            redirectAttributes.addFlashAttribute("success", "Event deleted.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to delete event: " + e.getMessage());
        }
        return "redirect:/admin/events";
    }

    @GetMapping("/venues")
    public String manageVenues(Model model) {
        model.addAttribute("venues", venueService.getAllVenues());
        model.addAttribute("venue", new Venue());
        model.addAttribute("venueTypes", VenueType.values());
        return "admin/venues";
    }

    @PostMapping("/venues/add")
    public String addVenue(@ModelAttribute Venue venue, RedirectAttributes redirectAttributes) {
        venueService.saveVenue(venue);
        redirectAttributes.addFlashAttribute("success", "Venue added successfully.");
        return "redirect:/admin/venues";
    }

    @PostMapping("/venues/{id}/toggle")
    public String toggleVenue(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        venueService.getVenueById(id).ifPresent(venue -> {
            venue.setAvailable(!venue.isAvailable());
            venueService.saveVenue(venue);
        });
        redirectAttributes.addFlashAttribute("success", "Venue availability updated.");
        return "redirect:/admin/venues";
    }

    @PostMapping("/venues/{id}/delete")
    public String deleteVenue(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        venueService.deleteVenue(id);
        redirectAttributes.addFlashAttribute("success", "Venue deleted.");
        return "redirect:/admin/venues";
    }

    @GetMapping("/users")
    public String manageUsers(Model model) {
        model.addAttribute("users", userService.getAllUsers());
        return "admin/users";
    }

    @PostMapping("/users/{id}/delete")
    public String deleteUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        userService.deleteUser(id);
        redirectAttributes.addFlashAttribute("success", "User deleted.");
        return "redirect:/admin/users";
    }
}
