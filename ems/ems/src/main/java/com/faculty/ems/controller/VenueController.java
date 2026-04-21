package com.faculty.ems.controller;

import com.faculty.ems.model.Venue;
import com.faculty.ems.service.VenueService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.List;

/**
 * VenueController - Handles HTTP requests for Venue Management
 * 
 * REQUEST FLOW:
 * User makes HTTP request (GET/POST)
 *          ↓
 * Spring Route matches to @RequestMapping endpoint
 *          ↓
 * Controller method is called
 *          ↓
 * Call VenueService for business logic
 *          ↓
 * Add data to Model (for Thymeleaf template)
 *          ↓
 * Return template name (HTML page)
 *          ↓
 * Browser displays rendered page
 * 
 * WHY REQUIRED:
 * 1. ADMIN ONLY ACCESS - Check authority to ensure only admins access venue management
 * 2. HTTP ROUTING - Maps URLs to Java methods
 * 3. DATA BINDING - Converts form data to Java objects (@ModelAttribute)
 * 4. ERROR HANDLING - Catches exceptions and shows user-friendly messages
 */
@Controller
@RequestMapping("/venues")
@RequiredArgsConstructor
public class VenueController {

    private final VenueService venueService;

    // ========== HELPER METHOD ==========
    /**
     * Check if current user is ADMIN
     * Used to restrict endpoints to admins only
     */
    private void requireAdminRole(UserDetails currentUser) {
        boolean isAdmin = currentUser.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (!isAdmin) {
            throw new RuntimeException("Access Denied: Admin role required");
        }
    }

    // ========== GET ALL VENUES LIST ==========
    /**
     * ENDPOINT: GET /venues
     * WHAT IT DOES: Display all venues
     * 
     * FOR ADMINS: Shows all venues (active + inactive)
     * FOR USERS: Shows only active venues
     * 
     * RESPONSE: venues/list.html template with list of venues
     */
    @GetMapping
    public String listVenues(Model model,
                            @AuthenticationPrincipal UserDetails currentUser) {
        List<Venue> venues;

        // Check if user is admin
        boolean isAdmin = currentUser.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (isAdmin) {
            // Admin sees ALL venues
            venues = venueService.getAllVenues();
            model.addAttribute("isAdmin", true);
        } else {
            // Regular user sees only ACTIVE venues
            venues = venueService.getAllActiveVenues();
            model.addAttribute("isAdmin", false);
        }

        model.addAttribute("venues", venues);
        return "venues/list";
    }

    // ========== CREATE VENUE FORM ==========
    /**
     * ENDPOINT: GET /venues/new
     * WHAT IT DOES: Display blank form to create new venue
     * 
     * ADMIN ONLY
     * 
     * RESPONSE: venues/form.html with empty Venue object
     */
    @GetMapping("/new")
    public String showCreateForm(Model model,
                                @AuthenticationPrincipal UserDetails currentUser) {
        // Check authorization
        requireAdminRole(currentUser);

        // Create empty venue object for form binding
        model.addAttribute("venue", new Venue());
        model.addAttribute("isNew", true);  // Flag to show "Create" button not "Update"

        return "venues/form";
    }

    // ========== SUBMIT CREATE VENUE ==========
    /**
     * ENDPOINT: POST /venues/new
     * WHAT IT DOES: Save new venue to database
     * 
     * FLOW:
     * 1. User fills form and clicks Submit
     * 2. Form data is bound to Venue object (@ModelAttribute)
     * 3. VenueService.createVenue() validates and saves
     * 4. Success message added to redirect attributes (flash message)
     * 5. Redirect to venues list page
     * 
     * ADMIN ONLY
     * 
     * EXCEPTION HANDLING: Catches IllegalArgumentException and returns form with error
     */
    @PostMapping("/new")
    public String createVenue(@ModelAttribute Venue venue,
                             @AuthenticationPrincipal UserDetails currentUser,
                             RedirectAttributes ra,
                             Model model) {
        // Check authorization
        requireAdminRole(currentUser);

        try {
            // Call service to validate and save
            venueService.createVenue(venue);

            // Add success message (flash attribute = shown once after redirect)
            ra.addFlashAttribute("success", "Venue '" + venue.getName() + "' created successfully!");

            // Redirect to list page
            return "redirect:/venues";

        } catch (IllegalArgumentException e) {
            // Validation failed - return form with error message
            model.addAttribute("error", e.getMessage());
            model.addAttribute("venue", venue);
            model.addAttribute("isNew", true);
            return "venues/form";
        }
    }

    // ========== VIEW VENUE DETAILS ==========
    /**
     * ENDPOINT: GET /venues/{id}
     * WHAT IT DOES: Display detailed view of single venue
     * 
     * RESPONSE: venues/detail.html with venue information
     */
    @GetMapping("/{id}")
    public String viewVenue(@PathVariable Long id, Model model) {
        try {
            Venue venue = venueService.getVenueById(id);
            model.addAttribute("venue", venue);
            return "venues/detail";

        } catch (RuntimeException e) {
            model.addAttribute("error", "Venue not found");
            return "venues/list";
        }
    }

    // ========== EDIT VENUE FORM ==========
    /**
     * ENDPOINT: GET /venues/{id}/edit
     * WHAT IT DOES: Display form with existing venue data for editing
     * 
     * FLOW:
     * 1. Admin clicks Edit button on venue card
     * 2. Fetch venue from database by ID
     * 3. Populate form with current values
     * 4. User modifies fields
     * 5. Submit goes to POST handler
     * 
     * ADMIN ONLY
     * 
     * RESPONSE: venues/form.html with pre-filled Venue object
     */
    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id,
                              Model model,
                              @AuthenticationPrincipal UserDetails currentUser) {
        // Check authorization
        requireAdminRole(currentUser);

        try {
            Venue venue = venueService.getVenueById(id);
            model.addAttribute("venue", venue);
            model.addAttribute("isNew", false);  // Flag for "Update" button
            model.addAttribute("venueId", id);

            return "venues/form";

        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            return "redirect:/venues";
        }
    }

    // ========== SUBMIT UPDATE VENUE ==========
    /**
     * ENDPOINT: POST /venues/{id}/edit
     * WHAT IT DOES: Save updated venue to database
     * 
     * FLOW:
     * 1. User modifies form fields and clicks Update
     * 2. Form data bound to Venue object
     * 3. VenueService.updateVenue() updates only changed fields
     * 4. Save to database
     * 5. Redirect to list with success message
     * 
     * ADMIN ONLY
     */
    @PostMapping("/{id}/edit")
    public String updateVenue(@PathVariable Long id,
                             @ModelAttribute Venue venue,
                             @AuthenticationPrincipal UserDetails currentUser,
                             RedirectAttributes ra,
                             Model model) {
        // Check authorization
        requireAdminRole(currentUser);

        try {
            venueService.updateVenue(id, venue);
            ra.addFlashAttribute("success", "Venue updated successfully!");
            return "redirect:/venues";

        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("venue", venue);
            model.addAttribute("isNew", false);
            model.addAttribute("venueId", id);
            return "venues/form";
        }
    }

    // ========== DEACTIVATE VENUE ==========
    /**
     * ENDPOINT: POST /venues/{id}/deactivate
     * WHAT IT DOES: Soft delete - mark venue as inactive
     * 
     * WHY NOT HARD DELETE:
     * - Keeps booking history intact
     * - Can reactivate if needed
     * - No data loss
     * 
     * ADMIN ONLY
     * 
     * RESPONSE: Redirect to list with success message
     */
    @PostMapping("/{id}/deactivate")
    public String deactivateVenue(@PathVariable Long id,
                                 @AuthenticationPrincipal UserDetails currentUser,
                                 RedirectAttributes ra) {
        // Check authorization
        requireAdminRole(currentUser);

        try {
            venueService.deactivateVenue(id);
            ra.addFlashAttribute("success", "Venue deactivated successfully!");
            return "redirect:/venues";

        } catch (RuntimeException e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/venues";
        }
    }

    // ========== REACTIVATE VENUE ==========
    /**
     * ENDPOINT: POST /venues/{id}/activate
     * WHAT IT DOES: Mark deactivated venue as active again
     * 
     * ADMIN ONLY
     * 
     * RESPONSE: Redirect to list with success message
     */
    @PostMapping("/{id}/activate")
    public String reactivateVenue(@PathVariable Long id,
                                 @AuthenticationPrincipal UserDetails currentUser,
                                 RedirectAttributes ra) {
        // Check authorization
        requireAdminRole(currentUser);

        try {
            venueService.reactivateVenue(id);
            ra.addFlashAttribute("success", "Venue reactivated successfully!");
            return "redirect:/venues";

        } catch (RuntimeException e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/venues";
        }
    }

    // ========== DELETE VENUE ==========
    /**
     * ENDPOINT: POST /venues/{id}/delete
     * WHAT IT DOES: Permanently delete venue from database
     * 
     * WARNING: This removes all venue data permanently
     * Better to use deactivateVenue() for normal operations
     * 
     * ADMIN ONLY
     * 
     * RESPONSE: Redirect to list with success message
     */
    @PostMapping("/{id}/delete")
    public String deleteVenue(@PathVariable Long id,
                             @AuthenticationPrincipal UserDetails currentUser,
                             RedirectAttributes ra) {
        // Check authorization
        requireAdminRole(currentUser);

        try {
            venueService.deleteVenue(id);
            ra.addFlashAttribute("success", "Venue deleted permanently!");
            return "redirect:/venues";

        } catch (RuntimeException e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/venues";
        }
    }
}
