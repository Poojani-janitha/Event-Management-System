package com.faculty.ems.strategy.impl;

import com.faculty.ems.dto.VenueFilterCriteria;
import com.faculty.ems.model.Venue;
import com.faculty.ems.strategy.VenueFilterStrategy;
import org.springframework.stereotype.Component;

/**
 * Concrete Strategy: Filters venues by active status.
 */
@Component
public class StatusFilterStrategy implements VenueFilterStrategy {
    @Override
    public boolean matches(Venue venue, VenueFilterCriteria criteria) {
        Boolean active = criteria.getActive();
        // If status is null (e.g. "All" selected), the strategy is skipped
        if (active == null) {
            return true;
        }
        
        return venue.getActive().equals(active);
    }
}
