package com.eventmanagement.config;

import com.eventmanagement.model.Role;
import com.eventmanagement.model.User;
import com.eventmanagement.model.Venue;
import com.eventmanagement.model.VenueType;
import com.eventmanagement.repository.UserRepository;
import com.eventmanagement.repository.VenueRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VenueRepository venueRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (!userRepository.existsByEmail("admin@university.edu")) {
            User admin = new User();
            admin.setName("System Administrator");
            admin.setEmail("admin@university.edu");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRole(Role.ADMIN);
            admin.setSociety("Administration");
            userRepository.save(admin);
        }

        if (!userRepository.existsByEmail("manager@university.edu")) {
            User manager = new User();
            manager.setName("Society Manager");
            manager.setEmail("manager@university.edu");
            manager.setPassword(passwordEncoder.encode("manager123"));
            manager.setRole(Role.SOCIETY_MANAGER);
            manager.setSociety("Computing Society");
            userRepository.save(manager);
        }

        if (venueRepository.count() == 0) {
            Venue auditorium = new Venue();
            auditorium.setName("Main Auditorium");
            auditorium.setType(VenueType.AUDITORIUM);
            auditorium.setCapacity(500);
            auditorium.setDescription("Large auditorium suitable for conferences, seminars and cultural events.");
            auditorium.setAvailable(true);
            venueRepository.save(auditorium);

            Venue seminarHall1 = new Venue();
            seminarHall1.setName("Seminar Hall A");
            seminarHall1.setType(VenueType.SEMINAR_HALL);
            seminarHall1.setCapacity(100);
            seminarHall1.setDescription("Medium-sized seminar hall for presentations and workshops.");
            seminarHall1.setAvailable(true);
            venueRepository.save(seminarHall1);

            Venue seminarHall2 = new Venue();
            seminarHall2.setName("Seminar Hall B");
            seminarHall2.setType(VenueType.SEMINAR_HALL);
            seminarHall2.setCapacity(80);
            seminarHall2.setDescription("Seminar hall equipped with projector and audio system.");
            seminarHall2.setAvailable(true);
            venueRepository.save(seminarHall2);

            Venue lab1 = new Venue();
            lab1.setName("Computer Lab 1");
            lab1.setType(VenueType.LAB);
            lab1.setCapacity(40);
            lab1.setDescription("Computer lab with 40 workstations for technical sessions.");
            lab1.setAvailable(true);
            venueRepository.save(lab1);

            Venue lab2 = new Venue();
            lab2.setName("Computer Lab 2");
            lab2.setType(VenueType.LAB);
            lab2.setCapacity(35);
            lab2.setDescription("Computer lab for coding sessions and hackathons.");
            lab2.setAvailable(true);
            venueRepository.save(lab2);
        }
    }
}
