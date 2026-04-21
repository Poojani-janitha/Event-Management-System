package com.faculty.ems.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.boot.model.internal.XMLContext;

import java.sql.Timestamp;

@Data
@Entity
@Table(name = "venues")
public class Venue {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false)
    private String location;

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
