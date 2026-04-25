package com.faculty.ems.strategy.impl;

import com.faculty.ems.dto.VenueFilterCriteria;
import com.faculty.ems.model.Venue;
import com.faculty.ems.strategy.VenueFilterStrategy;
import org.springframework.stereotype.Component;

/**
 * Concrete Strategy: Filters venues by minimum capacity.
 */
@Component
public class CapacityFilterStrategy implements VenueFilterStrategy {
    @Override
    public boolean matches(Venue venue, VenueFilterCriteria criteria) {
        Integer minCapacity = criteria.getMinCapacity();
        // If no minimum capacity is provided, the strategy is skipped
        if (minCapacity == null) {
            return true;
        }
        
        return venue.getCapacity() >= minCapacity;
    }
}
