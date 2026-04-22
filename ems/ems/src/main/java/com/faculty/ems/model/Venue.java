package com.faculty.ems.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;


import java.sql.Timestamp;

@Data
@Entity
@Table(name = "venues")
public class Venue {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Venue name is required")
    @Column(nullable = false, unique = true)
    private String name;

    @NotBlank(message = "Venue location is required")
    @Column(nullable = false)
    private String location;

    @Min(value = 1, message = "Venue capacity must be greater than 0")
    @Column(nullable = false)
    private Integer capacity;

    @Column(nullable = false)
    private Boolean has_projector = false;

    @Column(nullable = false)
    private Boolean has_ac = false;

    @Column(nullable = false)
    private Boolean has_sound = false;


    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "active")
    private Boolean active = true;

    @org.hibernate.annotations.CreationTimestamp
    @Column(name = "created_at",updatable = false)
    private Timestamp created_at;
}
