package com.faculty.ems.service;

import com.faculty.ems.dto.BookingFilterCriteria;
import com.faculty.ems.model.VenueBooking;
import com.faculty.ems.strategy.BookingFilterStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service to apply all active booking filter strategies.
 */
@Service
public class BookingFilterService {

    private final List<BookingFilterStrategy> strategies;

    @Autowired
    public BookingFilterService(List<BookingFilterStrategy> strategies) {
        this.strategies = strategies;
    }

    public List<VenueBooking> filter(List<VenueBooking> bookings, BookingFilterCriteria criteria) {
        return bookings.stream()
                .filter(booking -> strategies.stream().allMatch(strategy -> strategy.matches(booking, criteria)))
                .collect(Collectors.toList());
    }
}
