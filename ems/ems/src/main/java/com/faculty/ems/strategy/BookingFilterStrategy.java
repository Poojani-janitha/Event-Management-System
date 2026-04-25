package com.faculty.ems.strategy;

import com.faculty.ems.dto.BookingFilterCriteria;
import com.faculty.ems.model.VenueBooking;

/**
 * Strategy Interface for venue booking filtering.
 */
public interface BookingFilterStrategy {
    boolean matches(VenueBooking booking, BookingFilterCriteria criteria);
}
