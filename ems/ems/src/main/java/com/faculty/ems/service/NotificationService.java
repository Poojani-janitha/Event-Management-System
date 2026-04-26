package com.faculty.ems.service;

import com.faculty.ems.dto.HeaderNotification;
import com.faculty.ems.model.Event;
import com.faculty.ems.model.SocietyAdminRequest;
import com.faculty.ems.model.User;
import com.faculty.ems.model.VenueBooking;
import com.faculty.ems.repository.EventRepository;
import com.faculty.ems.repository.SocietyAdminRequestRepository;
import com.faculty.ems.repository.VenueBookingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final VenueBookingRepository venueBookingRepository;
    private final SocietyAdminRequestRepository societyAdminRequestRepository;
    private final EventRepository eventRepository;

    public List<HeaderNotification> getNotificationsForUser(User user) {
        List<HeaderNotification> notifications = new ArrayList<>();

        if (user == null) {
            return notifications;
        }

        if (user.getRole().name().equals("ADMIN")) {
            addAdminNotifications(notifications);
        } else {
            addUserNotifications(notifications, user);
        }

        addUpcomingEventNotifications(notifications);

        if (notifications.size() > 10) {
            return notifications.subList(0, 10);
        }
        return notifications;
    }

    private void addAdminNotifications(List<HeaderNotification> notifications) {
        List<VenueBooking> pendingBookings = venueBookingRepository.findByStatus(VenueBooking.BookingStatus.PENDING);
        if (!pendingBookings.isEmpty()) {
            notifications.add(new HeaderNotification(
                    "Pending Bookings",
                    pendingBookings.size() + " booking request(s) waiting for approval.",
                    "/venues/requests",
                    "warning"
            ));
        }

        List<SocietyAdminRequest> pendingRequests = societyAdminRequestRepository
                .findByStatusOrderByCreatedAtDesc(SocietyAdminRequest.RequestStatus.PENDING);
        if (!pendingRequests.isEmpty()) {
            notifications.add(new HeaderNotification(
                    "Society Admin Requests",
                    pendingRequests.size() + " society admin request(s) pending review.",
                    "/dashboard",
                    "info"
            ));
        }

        for (Event event : eventRepository.findTop5ByOrderByIdDesc()) {
            notifications.add(new HeaderNotification(
                    "New Event Added",
                    event.getTitle(),
                    "/events/" + event.getId(),
                    "primary"
            ));
        }
    }

    // load the notification whitch are lerevant to the each user
    private void addUserNotifications(List<HeaderNotification> notifications, User user) {
        List<VenueBooking> myBookings = venueBookingRepository.findByRequestedById(user.getId());
        myBookings.stream()
                .filter(b -> b.getStatus() == VenueBooking.BookingStatus.APPROVED
                        || b.getStatus() == VenueBooking.BookingStatus.REJECTED
                        || b.getStatus() == VenueBooking.BookingStatus.CANCELLED)
                .sorted((a, b) -> b.getBookingDate().compareTo(a.getBookingDate()))
                .limit(5)
                .forEach(booking -> notifications.add(new HeaderNotification(
                        "Booking " + booking.getStatus(),
                        booking.getEvent().getTitle() + " at " + booking.getVenue().getName(),
                        "/bookings/my",
                        booking.getStatus() == VenueBooking.BookingStatus.APPROVED ? "success" : "danger"
                )));

        List<SocietyAdminRequest> reviewed = societyAdminRequestRepository
                .findReviewedByUser(user.getId(), SocietyAdminRequest.RequestStatus.PENDING);
        reviewed.stream().limit(5).forEach(req -> notifications.add(new HeaderNotification(
                "Admin Request " + req.getStatus(),
                req.getSocietyName(),
                "/users/society-admin-request",
                req.getStatus() == SocietyAdminRequest.RequestStatus.APPROVED ? "success" : "danger"
        )));
    }

    private void addUpcomingEventNotifications(List<HeaderNotification> notifications) {
        LocalDate today = LocalDate.now();
        LocalDate nearDate = today.plusDays(3);

        List<VenueBooking> upcoming = venueBookingRepository.findByBookingDateBetweenAndStatus(
                today,
                nearDate,
                VenueBooking.BookingStatus.APPROVED
        );

        upcoming.stream().limit(3).forEach(booking -> notifications.add(new HeaderNotification(
                "Upcoming Event",
                booking.getEvent().getTitle() + " on " + booking.getBookingDate(),
                "/events/" + booking.getEvent().getId(),
                "secondary"
        )));
    }
}
