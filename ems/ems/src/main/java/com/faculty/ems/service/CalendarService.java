package com.faculty.ems.service;

import com.faculty.ems.dto.CalendarDto;
import com.faculty.ems.model.VenueBooking;
import com.faculty.ems.model.VenueBooking.BookingStatus;
import com.faculty.ems.repository.VenueBookingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CalendarService {

    @Autowired
    private VenueBookingRepository venueBookingRepository;

    /**
     * Builds a CalendarDto for the given venue, year, and month.
     * Only APPROVED bookings are shown on the calendar.
     */
    public CalendarDto getCalendarData(Long venueId, int year, int month) {

        YearMonth yearMonth = YearMonth.of(year, month);

        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate   = yearMonth.atEndOfMonth();

        // Fetch approved bookings in the date range
        List<VenueBooking> bookings = venueBookingRepository
                .findByVenueIdAndBookingDateBetweenAndStatus(
                        venueId, startDate, endDate, BookingStatus.APPROVED);

        // Group by day-of-month  e.g. { 3 -> [...], 15 -> [...] }
        Map<Integer, List<VenueBooking>> bookingsByDay = bookings.stream()
                .collect(Collectors.groupingBy(
                        b -> b.getBookingDate().getDayOfMonth()
                ));

        CalendarDto dto = new CalendarDto();
        dto.setVenueId(venueId);
        dto.setYear(year);
        dto.setMonth(month);
        dto.setBookingsByDay(bookingsByDay);
        dto.setDaysInMonth(yearMonth.lengthOfMonth());

        // ISO: Monday=1 … Sunday=7.  We want Monday as column 0.
        dto.setStartDayOfWeek(startDate.getDayOfWeek().getValue()); // 1-7

        return dto;
    }

    public CalendarDto getGlobalCalendarData(int year, int month) {

        YearMonth yearMonth = YearMonth.of(year, month);

        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate   = yearMonth.atEndOfMonth();

        // Fetch ALL approved bookings in the date range globally
        List<VenueBooking> bookings = venueBookingRepository
                .findByBookingDateBetweenAndStatus(
                        startDate, endDate, BookingStatus.APPROVED);

        // Group by day-of-month
        Map<Integer, List<VenueBooking>> bookingsByDay = bookings.stream()
                .collect(Collectors.groupingBy(
                        b -> b.getBookingDate().getDayOfMonth()
                ));

        CalendarDto dto = new CalendarDto();
        dto.setVenueId(null);
        dto.setYear(year);
        dto.setMonth(month);
        dto.setBookingsByDay(bookingsByDay);
        dto.setDaysInMonth(yearMonth.lengthOfMonth());
        dto.setStartDayOfWeek(startDate.getDayOfWeek().getValue()); // 1-7

        return dto;
    }
}