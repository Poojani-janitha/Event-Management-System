package com.faculty.ems.strategy;

import com.faculty.ems.dto.VenueFilterCriteria;
import com.faculty.ems.model.Venue;

/**
 * Strategy Design Pattern: This interface defines the contract for any venue filtering strategy.
 * Each implementation will handle a specific filtering concern.
 */
public interface VenueFilterStrategy {
    /**
     * Determines if the given venue matches the criteria handled by this strategy.
     * 
     * @param venue The venue to check
     * @param criteria The filter criteria provided by the user
     * @return true if the venue matches, false otherwise
     */
    boolean matches(Venue venue, VenueFilterCriteria criteria);
}
