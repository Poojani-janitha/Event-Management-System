package com.faculty.ems.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CalendarBookingItemDto {
    private String eventTitle;
    private String venueName;
    private String startTime;
    private String endTime;
    private String societyName;
    private String status;
    private String adminNote;
}

