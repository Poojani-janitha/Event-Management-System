package com.faculty.ems.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VenueDto {

    private Long id;

    @NotBlank(message = "Venue name is required")
    private String name;

    @NotBlank(message = "Venue location is required")
    private String location;

    @Min(value = 1, message = "Venue capacity must be greater than 0")
    private int capacity;

    private boolean active;

    private String description;

    private boolean has_projector;

    private boolean has_ac;

    private boolean has_sound;

    // Response/Message fields
    private String message;

    private String status; // "success", "error"

    private String messageType; // "success", "danger"
}