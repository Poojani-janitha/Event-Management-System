package com.faculty.ems.service;

import com.faculty.ems.dto.VenueFilterCriteria;
import com.faculty.ems.model.Venue;
import com.faculty.ems.strategy.VenueFilterStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Strategy Design Pattern Context: This service acts as the context that applies
 * all available filter strategies to a list of venues.
 */
@Service
public class VenueFilterService {

    private final List<VenueFilterStrategy> strategies;

    /**
     * Spring automatically injects all beans implementing VenueFilterStrategy into this list.
     * This is a clean way to collect all strategies without hardcoding them.
     */
    @Autowired
    public VenueFilterService(List<VenueFilterStrategy> strategies) {
        this.strategies = strategies;
    }

    /**
     * Applies all active strategies to the given list of venues.
     * Only venues that match ALL active strategies are returned.
     * 
     * @param venues The list of venues to filter
     * @param criteria The filter criteria
     * @return A filtered list of venues
     */
    public List<Venue> filter(List<Venue> venues, VenueFilterCriteria criteria) {
        return venues.stream()
                .filter(venue -> strategies.stream().allMatch(strategy -> strategy.matches(venue, criteria)))
                .collect(Collectors.toList());
    }
}
