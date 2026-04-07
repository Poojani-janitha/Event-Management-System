package com.faculty.ems.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // Unique user identifier [cite: 81, 211]

    @Column(unique = true, nullable = false, length = 50)
    private String username; // Login username [cite: 81, 214]

    @Column(unique = true, nullable = false, length = 100)
    private String email; // User email address [cite: 81, 216]

    @Column(nullable = false)
    private String password; // BCrypt hashed password [cite: 81, 218]

    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName; // Display name [cite: 81, 221]

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role = Role.MEMBER; // ADMIN, SOCIETY_ADMIN, or MEMBER

    private boolean enabled = true; // Account active flag [cite: 81, 225]

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt; // Registration timestamp [cite: 81, 228]

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt; // Last update timestamp [cite: 81]
}