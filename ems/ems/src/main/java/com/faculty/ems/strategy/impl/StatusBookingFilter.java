package com.faculty.ems.strategy.impl;

import com.faculty.ems.dto.BookingFilterCriteria;
import com.faculty.ems.model.VenueBooking;
import com.faculty.ems.strategy.BookingFilterStrategy;
import org.springframework.stereotype.Component;

@Component
public class StatusBookingFilter implements BookingFilterStrategy {
    @Override
    public boolean matches(VenueBooking booking, BookingFilterCriteria criteria) {
        if (criteria.getStatus() == null || criteria.getStatus().isEmpty() || criteria.getStatus().equals("ALL")) {
            return true;
        }
        return booking.getStatus().name().equals(criteria.getStatus());
    }
}
