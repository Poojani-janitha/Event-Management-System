package com.faculty.ems.strategy.impl;

import com.faculty.ems.dto.BookingFilterCriteria;
import com.faculty.ems.model.VenueBooking;
import com.faculty.ems.strategy.BookingFilterStrategy;
import org.springframework.stereotype.Component;

@Component
public class SocietyBookingFilter implements BookingFilterStrategy {
    @Override
    public boolean matches(VenueBooking booking, BookingFilterCriteria criteria) {
        if (criteria.getSocietyId() == null) return true;
        return booking.getSociety().getId().equals(criteria.getSocietyId());
    }
}
