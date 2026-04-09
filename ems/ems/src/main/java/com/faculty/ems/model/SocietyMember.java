package com.faculty.ems.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "society_members")
@Data
@NoArgsConstructor
public class SocietyMember {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "society_id", nullable = false)
    private Society society;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    private RoleInSociety roleInSociety = RoleInSociety.MEMBER; //ADMIN or MEMBER

    @CreationTimestamp
    private LocalDateTime joinedAt;
}
