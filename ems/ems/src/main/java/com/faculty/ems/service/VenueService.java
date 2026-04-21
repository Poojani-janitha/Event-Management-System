package com.faculty.ems.service;

import com.faculty.ems.model.Venue;
import com.faculty.ems.repository.VenueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

/**
 * VenueService - Business logic for Venue Management
 * 
 * WHAT IT DOES:
 * - Handles all venue CRUD operations (Create, Read, Update, Delete/Deactivate)
 * - Validates venue data before saving
 * - Ensures data consistency and business rules
 * 
 * WHY IT EXISTS:
 * - Keeps business logic separate from web layer (Controller)
 * - Makes testing easier
 * - Allows reuse of logic from different controllers
 */
@Service
@RequiredArgsConstructor
public class VenueService {

    private final VenueRepository venueRepository;

    /**
     * GET ALL VENUES - Active only
     * Used for: Booking form, Calendar view, List page
     */
    public List<Venue> getAllActiveVenues() {
        return venueRepository.findByActiveTrue();
    }

    /**
     * GET ALL VENUES - Including inactive (Admin only)
     * Used for: Admin management page to see all venues
     */
    public List<Venue> getAllVenues() {
        return venueRepository.findAll();
    }

    /**
     * GET SINGLE VENUE BY ID
     * Used for: Detail page, Edit form population
     */
    public Venue getVenueById(Long id) {
        return venueRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Venue not found with id: " + id));
    }

    /**
     * CREATE NEW VENUE
     * Input: Venue object with name, location, capacity, facilities
     * Output: Saved venue with generated ID
     * Used by: VenueController POST /venues/new
     */
    public Venue createVenue(Venue venue) {
        // Validation: Name must not be empty
        if (venue.getName() == null || venue.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Venue name cannot be empty");
        }

        // Validation: Location must not be empty
        if (venue.getLocation() == null || venue.getLocation().trim().isEmpty()) {
            throw new IllegalArgumentException("Venue location cannot be empty");
        }

        // Validation: Capacity must be positive
        if (venue.getCapacity() == null || venue.getCapacity() <= 0) {
            throw new IllegalArgumentException("Venue capacity must be greater than 0");
        }

        // Set new venues to active by default
        if (venue.getActive() == null) {
            venue.setActive(true);
        }

        return venueRepository.save(venue);
    }

    /**
     * UPDATE EXISTING VENUE
     * Input: Venue ID and updated venue data
     * Output: Updated venue
     * Used by: VenueController POST /venues/{id}/edit
     * 
     * IMPORTANT: Only updates fields that have changed
     * Preserves creation_date and other metadata
     */
    public Venue updateVenue(Long id, Venue updatedVenue) {
        // Fetch existing venue
        Venue existing = getVenueById(id);

        // Validation: Name must not be empty
        if (updatedVenue.getName() != null && !updatedVenue.getName().trim().isEmpty()) {
            existing.setName(updatedVenue.getName());
        }

        // Update location
        if (updatedVenue.getLocation() != null && !updatedVenue.getLocation().trim().isEmpty()) {
            existing.setLocation(updatedVenue.getLocation());
        }

        // Update capacity
        if (updatedVenue.getCapacity() != null && updatedVenue.getCapacity() > 0) {
            existing.setCapacity(updatedVenue.getCapacity());
        }

        // Update description
        if (updatedVenue.getDescription() != null) {
            existing.setDescription(updatedVenue.getDescription());
        }

        // Update facilities
        if (updatedVenue.getHas_ac() != null) {
            existing.setHas_ac(updatedVenue.getHas_ac());
        }
        if (updatedVenue.getHas_projector() != null) {
            existing.setHas_projector(updatedVenue.getHas_projector());
        }
        if (updatedVenue.getHas_sound() != null) {
            existing.setHas_sound(updatedVenue.getHas_sound());
        }

        return venueRepository.save(existing);
    }

    /**
     * DEACTIVATE VENUE (Soft Delete)
     * Instead of deleting venue from database, mark as inactive
     * 
     * WHY: Keeps historical data for reports and booking history
     * Prevents data loss
     * 
     * Used by: VenueController POST /venues/{id}/deactivate
     */
    public void deactivateVenue(Long id) {
        Venue venue = getVenueById(id);
        venue.setActive(false);
        venueRepository.save(venue);
    }

    /**
     * REACTIVATE VENUE
     * Mark a deactivated venue as active again
     * Used by: Admin management (optional feature)
     */
    public void reactivateVenue(Long id) {
        Venue venue = getVenueById(id);
        venue.setActive(true);
        venueRepository.save(venue);
    }

    /**
     * DELETE VENUE (Hard Delete)
     * Completely remove venue from database
     * 
     * WARNING: Use only if venue was created by mistake
     * For normal deactivation, use deactivateVenue()
     * 
     * Used by: (Optional - may not be needed)
     */
    public void deleteVenue(Long id) {
        if (!venueRepository.existsById(id)) {
            throw new RuntimeException("Venue not found with id: " + id);
        }
        venueRepository.deleteById(id);
    }
}
