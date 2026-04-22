package com.faculty.ems.service;

import com.faculty.ems.model.VenueBooking;
import com.faculty.ems.repository.VenueBookingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DashboardService {

    @Autowired
    private VenueBookingRepository venueBookingRepository;

    public List<VenueBooking> getPendingBookings() {
        return venueBookingRepository.findByStatus(VenueBooking.BookingStatus.PENDING);
    }

    public List<VenueBooking> getApprovedBookings() {
        return venueBookingRepository.findByStatus(VenueBooking.BookingStatus.APPROVED);
    }

    public List<VenueBooking> getBookingsByUser(Integer userId) {
        return venueBookingRepository.findByRequestedById(userId);
    }
}
