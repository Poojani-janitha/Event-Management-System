package com.faculty.ems.service;

import com.faculty.ems.model.Venue;
import com.faculty.ems.model.VenueBooking;
import com.faculty.ems.repository.VenueRepository;
import com.faculty.ems.repository.VenueBookingRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class VenueService {
    
    @Autowired
    private VenueRepository venueRepository;

    @Autowired
    private VenueBookingRepository venueBookingRepository;

    public List<Venue> getAllVenues(){
        return venueRepository.findAll();
    }

    public List<Venue> getActiveVenues(){
        return venueRepository.findByActiveTrue();
    }

    public void saveVenue(Venue venue) {
        venueRepository.save(venue);
    }

    public Venue getVenueById(Long id) {
        Optional<Venue> optionalVenue = venueRepository.findById(id);
        return optionalVenue.orElse(null);
    }

    public void editVenue(Venue venue) {
        venueRepository.save(venue);
    }

    public void deleteVenue(Long id) {
        
        // Check if venue has any bookings
        List<VenueBooking> bookings = venueBookingRepository.findByVenueId(id);
        if (!bookings.isEmpty()) {
            // Build detailed error message with booking details
            StringBuilder message = new StringBuilder();
            message.append("Venue cannot be deleted because it has the following bookings:<br/><br/>");
            
            for (VenueBooking booking : bookings) {
                message.append("• Event: ").append(booking.getEvent().getTitle()).append("<br/>");
                message.append("&nbsp;&nbsp;Date: ").append(booking.getBookingDate()).append("<br/>");
                message.append("&nbsp;&nbsp;Time: ").append(booking.getStartTime()).append(" - ").append(booking.getEndTime()).append("<br/>");
                message.append("&nbsp;&nbsp;Status: ").append(booking.getStatus()).append("<br/>");
                message.append("<br/>");
            }
            
            message.append("Please cancel all bookings before deleting this venue.");
            throw new IllegalArgumentException(message.toString());
        }
        
        if (!venueRepository.existsById(id)) {
            throw new EntityNotFoundException("Venue not found with id: " + id);
        }
        venueRepository.deleteById(id);
    }

    public Map<String, Object> getBookingsForMonth(YearMonth yearMonth) {
        Map<String, Object> result = new HashMap<>();
        List<Venue> venues = getAllVenues();
        
        // Get first and last day of the month
        LocalDate firstDay = yearMonth.atDay(1);
        LocalDate lastDay = yearMonth.atEndOfMonth();
        
        // Map to store bookings count per date
        Map<String, Integer> bookingsPerDate = new HashMap<>();
        Map<String, List<Map<String, String>>> bookingDetailsPerDate = new HashMap<>();
        
        // Initialize all dates in the month
        for (LocalDate date = firstDay; !date.isAfter(lastDay); date = date.plusDays(1)) {
            String dateStr = date.toString();
            bookingsPerDate.put(dateStr, 0);
            bookingDetailsPerDate.put(dateStr, new ArrayList<>());
        }
        
        // Count bookings for each date
        for (Venue venue : venues) {
            List<VenueBooking> venueBookings = venueBookingRepository.findByVenueId(venue.getId());
            
            for (VenueBooking booking : venueBookings) {
                // Only count active bookings
                if (!booking.getStatus().equals(VenueBooking.BookingStatus.REJECTED) &&
                    !booking.getStatus().equals(VenueBooking.BookingStatus.CANCELLED)) {
                    
                    LocalDate bookingDate = booking.getBookingDate();
                    if (!bookingDate.isBefore(firstDay) && !bookingDate.isAfter(lastDay)) {
                        String dateStr = bookingDate.toString();
                        
                        // Increment booking count
                        bookingsPerDate.put(dateStr, bookingsPerDate.getOrDefault(dateStr, 0) + 1);
                        
                        // Add booking details
                        Map<String, String> bookingDetail = new LinkedHashMap<>();
                        bookingDetail.put("venue", venue.getName());
                        bookingDetail.put("event", booking.getEvent().getTitle());
                        bookingDetail.put("time", booking.getStartTime() + " - " + booking.getEndTime());
                        bookingDetail.put("status", booking.getStatus().toString());
                        bookingDetailsPerDate.get(dateStr).add(bookingDetail);
                    }
                }
            }
        }
        
        // Calculate availability status for each date
        Map<String, String> dateStatus = new HashMap<>();
        int totalVenues = venues.size();
        
        for (String date : bookingsPerDate.keySet()) {
            int bookedCount = bookingsPerDate.get(date);
            String status;
            String info;
            
            if (bookedCount == 0) {
                status = "available";
                info = "All Available";
            } else if (bookedCount >= totalVenues) {
                status = "booked";
                info = "Fully Booked";
            } else {
                status = "partial";
                info = bookedCount + "/" + totalVenues;
            }
            
            dateStatus.put(date, status);
        }
        
        result.put("dateStatus", dateStatus);
        result.put("bookingsPerDate", bookingsPerDate);
        result.put("bookingDetails", bookingDetailsPerDate);
        result.put("totalVenues", totalVenues);
        result.put("month", yearMonth.getMonthValue());
        result.put("year", yearMonth.getYear());
        
        return result;
    }

   


    

}
