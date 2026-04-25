package com.faculty.ems.strategy.impl;

import com.faculty.ems.dto.VenueFilterCriteria;
import com.faculty.ems.model.Venue;
import com.faculty.ems.strategy.VenueFilterStrategy;
import org.springframework.stereotype.Component;

/**
 * Concrete Strategy: Filters venues by name or location using a search term.
 */
@Component
public class SearchFilterStrategy implements VenueFilterStrategy {
    @Override
    public boolean matches(Venue venue, VenueFilterCriteria criteria) {
        String term = criteria.getSearchTerm();
        // If no search term is provided, the strategy is skipped (matches everything)
        if (term == null || term.trim().isEmpty()) {
            return true;
        }
        
        String lowerTerm = term.toLowerCase();
        return venue.getName().toLowerCase().contains(lowerTerm) || 
               venue.getLocation().toLowerCase().contains(lowerTerm);
    }
}
