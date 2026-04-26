package com.faculty.ems.config;

import com.faculty.ems.dto.HeaderNotification;
import com.faculty.ems.model.User;
import com.faculty.ems.service.NotificationService;
import com.faculty.ems.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Collections;
import java.util.List;

@Component
@ControllerAdvice
@RequiredArgsConstructor
public class GlobalModelAttributes {

    private final UserService userService;
    private final NotificationService notificationService;

    @ModelAttribute("headerNotifications")
    public List<HeaderNotification> headerNotifications() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            return Collections.emptyList();
        }

        User user = userService.findUserByUsername(auth.getName());
        return notificationService.getNotificationsForUser(user);
    }

    @ModelAttribute("headerNotificationCount")
    public Integer headerNotificationCount() {
        return headerNotifications().size();
    }

    @ModelAttribute("currentRequestUri")
    public String currentRequestUri(HttpServletRequest request) {
        return request != null ? request.getRequestURI() : "";
    }
}
