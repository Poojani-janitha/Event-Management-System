package com.faculty.ems.dto;

import com.faculty.ems.model.VenueBooking;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CalendarDto {

    private Long venueId;
    private int year;
    private int month;

    // Key = day-of-month (1..31), Value = bookings on that day
    private Map<Integer, List<VenueBooking>> bookingsByDay;

    // Total days in the selected month (used by Thymeleaf to render the grid)
    private int daysInMonth;

    // Day-of-week the month starts on (1=Mon .. 7=Sun, ISO)
    private int startDayOfWeek;
}