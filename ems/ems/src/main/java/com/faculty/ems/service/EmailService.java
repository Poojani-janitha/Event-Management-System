package com.faculty.ems.service;

import com.faculty.ems.model.VenueBooking;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendBookingStatusEmail(VenueBooking booking) {
        if (booking.getRequestedBy() == null || booking.getRequestedBy().getEmail() == null) {
            return;
        }

        String to = booking.getRequestedBy().getEmail();
        String subject = "Booking " + booking.getStatus() + ": " + booking.getEvent().getTitle();
        
        StringBuilder message = new StringBuilder();
        message.append("Dear ").append(booking.getRequestedBy().getFullName()).append(",\n\n");
        message.append("Your booking request for '").append(booking.getEvent().getTitle()).append("' has been ")
               .append(booking.getStatus()).append(".\n\n");
        
        message.append("Venue: ").append(booking.getVenue().getName()).append("\n");
        message.append("Date: ").append(booking.getBookingDate()).append("\n");
        message.append("Time: ").append(booking.getStartTime()).append(" - ").append(booking.getEndTime()).append("\n\n");

        if (booking.getAdminNote() != null && !booking.getAdminNote().isEmpty()) {
            message.append("Admin Note: ").append(booking.getAdminNote()).append("\n\n");
        }

        message.append("Thank you,\nFaculty Event Management Team");

        SimpleMailMessage mail = new SimpleMailMessage();
        mail.setTo(to);
        mail.setSubject(subject);
        mail.setText(message.toString());

        try {
            mailSender.send(mail);
        } catch (Exception e) {
            // Log the error but don't crash the application
            System.err.println("Failed to send email to " + to + ": " + e.getMessage());
        }
    }
}
