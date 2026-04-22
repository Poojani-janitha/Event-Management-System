package com.faculty.ems.dto;

import lombok.Data;

@Data
public class BookingCalendarDTO {
    private Long bookingId;
    private String bookingDate;      // "2026-04-22"
    private String startTime;        // "09:00"
    private String endTime;          // "12:00"
    private String status;           // "APPROVED"
    private String eventTitle;
    private String eventType;
    private String eventDescription;
    private String venueName;
    private String venueLocation;
    private String societyName;
}
