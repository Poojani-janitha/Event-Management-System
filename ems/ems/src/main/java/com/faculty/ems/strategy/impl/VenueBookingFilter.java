package com.faculty.ems.strategy.impl;

import com.faculty.ems.dto.BookingFilterCriteria;
import com.faculty.ems.model.VenueBooking;
import com.faculty.ems.strategy.BookingFilterStrategy;
import org.springframework.stereotype.Component;

@Component
public class VenueBookingFilter implements BookingFilterStrategy {
    @Override
    public boolean matches(VenueBooking booking, BookingFilterCriteria criteria) {
        if (criteria.getVenueId() == null) return true;
        return booking.getVenue().getId().equals(criteria.getVenueId());
    }
}
