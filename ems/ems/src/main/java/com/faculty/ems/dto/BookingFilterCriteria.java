package com.faculty.ems.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO to hold criteria for filtering venue bookings.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingFilterCriteria {
    private Long venueId;
    private Integer societyId;
    private String status; // String to handle "ALL" or specific enum names
}
