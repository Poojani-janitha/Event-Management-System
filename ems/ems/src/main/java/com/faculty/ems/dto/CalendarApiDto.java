package com.faculty.ems.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CalendarApiDto {
    private int year;
    private int month;
    private int daysInMonth;
    private int startDayOfWeek;

    // Key = day-of-month (1..31), Value = booking items for that day
    private Map<Integer, List<CalendarBookingItemDto>> bookingsByDay;
}

