package com.faculty.ems.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object to hold the criteria for filtering venues.
 * This object is passed to the filtering strategies to determine if a venue matches.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VenueFilterCriteria {
    private String searchTerm;   // For name or location matching
    private Integer minCapacity; // For minimum capacity matching
    private Boolean active;      // For status matching (null means "All")
}
