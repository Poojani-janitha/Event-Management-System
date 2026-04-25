package com.faculty.ems.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.AssertTrue;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class VenueBookingFormDto {

    @NotNull(message = "Please select an event")
    private Long eventId;

    @NotNull(message = "Please select a venue")
    private Long venueId;

    @NotNull(message = "Booking date is required")
    @FutureOrPresent(message = "Booking date cannot be in the past")
    private LocalDate bookingDate;

    @NotNull(message = "Start time is required")
    private LocalTime startTime;

    @NotNull(message = "End time is required")
    private LocalTime endTime;

    @AssertTrue(message = "End time must be after start time")
    public boolean isTimeRangeValid() {
        if (startTime == null || endTime == null) {
            return true;
        }
        return endTime.isAfter(startTime);
    }
}
