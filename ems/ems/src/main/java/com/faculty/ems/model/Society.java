package com.faculty.ems.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "societies")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Society {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(unique = true, nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "contact_email", nullable = false)
    private String contactEmail;

    private String logoUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_id", nullable = false)
    private User societyAdmin;

    @Builder.Default
    private boolean active = false;

    @CreationTimestamp
    private LocalDateTime createdAt;
}
