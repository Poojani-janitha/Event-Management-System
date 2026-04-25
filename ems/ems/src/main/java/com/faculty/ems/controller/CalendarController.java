package com.faculty.ems.controller;

import com.faculty.ems.dto.CalendarDto;
import com.faculty.ems.model.Venue;
import com.faculty.ems.service.CalendarService;
import com.faculty.ems.service.VenueService;
import com.faculty.ems.repository.VenueBookingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import java.time.YearMonth;
import java.util.stream.Collectors;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.Month;
import java.time.format.TextStyle;

import com.faculty.ems.model.VenueBooking;
import org.springframework.web.bind.annotation.ResponseBody;
import java.util.*;

@Controller
@RequestMapping("/calendar")
public class CalendarController {

    @Autowired
    private CalendarService calendarService;

    @Autowired
    private VenueBookingRepository venueBookingRepository;

    @Autowired
    private VenueService venueService;

    /**
     * GET /calendar
     *
     * Optional params: venueId, year, month
     * Defaults: first active venue, current year & month
     */
    @GetMapping
    public String viewCalendar(
            @RequestParam(required = false) Long venueId,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month,
            Model model) {

        // Default to current year/month if not provided
        LocalDate today = LocalDate.now();
        if (year  == null) year  = today.getYear();
        if (month == null) month = today.getMonthValue();

        // Default to first active venue if no venueId selected
        if (venueId == null) {
            venueId = venueService.getActiveVenues()
                    .stream().findFirst()
                    .map(v -> v.getId())
                    .orElse(null);
        }

        if (venueId == null) {
            // No venues exist yet
            model.addAttribute("noVenues", true);
            return "calendar/view";
        }

        Venue venue = venueService.getVenueById(venueId);
        if (venue == null) return "redirect:/calendar";

        CalendarDto calendarDto = calendarService.getCalendarData(venueId, year, month);

        // Navigation: previous and next month
        LocalDate current = LocalDate.of(year, month, 1);
        LocalDate prev    = current.minusMonths(1);
        LocalDate next    = current.plusMonths(1);

        model.addAttribute("calendar",    calendarDto);
        model.addAttribute("venue",       venue);
        model.addAttribute("allVenues",   venueService.getActiveVenues());
        model.addAttribute("monthName",   Month.of(month).getDisplayName(TextStyle.FULL, Locale.ENGLISH));
        model.addAttribute("prevYear",    prev.getYear());
        model.addAttribute("prevMonth",   prev.getMonthValue());
        model.addAttribute("nextYear",    next.getYear());
        model.addAttribute("nextMonth",   next.getMonthValue());
        model.addAttribute("today",       today.getDayOfMonth());
        model.addAttribute("todayYear",   today.getYear());
        model.addAttribute("todayMonth",  today.getMonthValue());

        return "calendar/view";
    }

    @GetMapping("/monthly-bookings")
    @ResponseBody
    public Map<String, Object> getBookingsForMonthAndYear(
            @RequestParam int year,
            @RequestParam int month) {

        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate   = yearMonth.atEndOfMonth();

        // Fetch all approved bookings for the date range
        List<VenueBooking> bookings = venueBookingRepository
                .findByBookingDateBetweenAndStatus(startDate, endDate, VenueBooking.BookingStatus.APPROVED);

        // Group by day-of-month
        Map<Integer, List<VenueBooking>> bookingsByDay = bookings.stream()
                .collect(Collectors.groupingBy(
                        b -> b.getBookingDate().getDayOfMonth()
                ));

        Map<Integer, List<Map<String, String>>> flat = new LinkedHashMap<>();

        bookingsByDay.forEach((day, dayBookings) -> {
            List<Map<String, String>> list = new ArrayList<>();
            for (VenueBooking b : dayBookings) {
                Map<String, String> entry = new LinkedHashMap<>();
                entry.put("eventTitle",  b.getEvent().getTitle());
                entry.put("startTime",   b.getStartTime().toString());
                entry.put("endTime",     b.getEndTime().toString());
                entry.put("societyName", b.getSociety().getName());
                entry.put("status",      b.getStatus().name());
                list.add(entry);
            }
            flat.put(day, list);
        });

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("bookingsByDay",  flat);
        response.put("daysInMonth",    yearMonth.lengthOfMonth());
        response.put("startDayOfWeek", startDate.getDayOfWeek().getValue());
        return response;
    }
}