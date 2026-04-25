package com.faculty.ems.controller;

import com.faculty.ems.model.Event;
import com.faculty.ems.model.Society;
import com.faculty.ems.model.User;
import com.faculty.ems.service.EventService;
import com.faculty.ems.repository.SocietyRepository;   
import com.faculty.ems.repository.UserRepository;       
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.validation.Valid;
import org.springframework.validation.BindingResult;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Controller
@RequestMapping("/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;
    private final SocietyRepository societyRepo;
    private final UserRepository userRepo;

    @GetMapping
    public String list(Model model,
                       @RequestParam(required = false) Integer societyId,
                       @AuthenticationPrincipal UserDetails currentUser) {
        User user = userRepo.findByUsername(currentUser.getUsername()).orElseThrow();
        boolean isAdmin = hasRole(currentUser, "ROLE_ADMIN");

        Map<Integer, Society> allowedSocieties = getAllowedSocieties(user, isAdmin);

        if (!isAdmin && allowedSocieties.isEmpty()) {
            model.addAttribute("events", List.of());
            model.addAttribute("societyOptions", List.of());
            model.addAttribute("selectedSocietyId", societyId);
            model.addAttribute("error", "You are not registered in any society yet.");
            return "events/list";
        }

        List<Event> events;
        if (societyId != null) {
            if (!isAdmin && !allowedSocieties.containsKey(societyId)) {
                throw new AccessDeniedException("You cannot access events for this society");
            }
            events = eventService.findBySociety(societyId.longValue());
        } else if (isAdmin) {
            events = eventService.findAll();
        } else {
            List<Long> allowedSocietyIds = allowedSocieties.keySet().stream().map(Integer::longValue).toList();
            events = eventService.findBySocietyIds(allowedSocietyIds);
        }

        model.addAttribute("events", events);
        model.addAttribute("societyOptions", new ArrayList<>(allowedSocieties.values()));
        model.addAttribute("selectedSocietyId", societyId);
        return "events/list";
    }

    @GetMapping("/new")
    public String createForm(Model model,
                             @RequestParam(required = false) Integer societyId,
                             @AuthenticationPrincipal UserDetails currentUser) {
        User user = userRepo.findByUsername(currentUser.getUsername()).orElseThrow();
        requireSocietyAdmin(currentUser);
        Map<Integer, Society> allowedSocieties = getAllowedSocieties(user, false);

        if (allowedSocieties.isEmpty()) {
            throw new AccessDeniedException("You are not registered in any society");
        }

        model.addAttribute("event", new Event());
        model.addAttribute("eventTypes", Event.EventType.values());
        model.addAttribute("societies", new ArrayList<>(allowedSocieties.values()));
        model.addAttribute("selectedSocietyId", societyId);
        model.addAttribute("selectedSocietyName", null);

        if (societyId != null) {
            if (!allowedSocieties.containsKey(societyId)) {
                throw new AccessDeniedException("You cannot create events for this society");
            }
            Event event = new Event();
            Society selectedSociety = societyRepo.findById(societyId).orElseThrow();
            event.setSociety(selectedSociety);
            model.addAttribute("event", event);
            model.addAttribute("selectedSocietyName", selectedSociety.getName());
        }
        return "events/form";
    }

    @PostMapping("/new")
    public String create(@Valid @ModelAttribute("event") Event event,
                         BindingResult result,
                         @RequestParam(required = false) Integer selectedSocietyId,
                         @AuthenticationPrincipal UserDetails currentUser,
                         Model model,
                         RedirectAttributes ra) {
        User user = userRepo.findByUsername(currentUser.getUsername()).orElseThrow();
        requireSocietyAdmin(currentUser);
        Map<Integer, Society> allowedSocieties = getAllowedSocieties(user, false);

        Integer effectiveSocietyId = event.getSociety() != null ? event.getSociety().getId() : selectedSocietyId;

        if (result.hasErrors()) {
            model.addAttribute("eventTypes", Event.EventType.values());
            model.addAttribute("societies", new ArrayList<>(allowedSocieties.values()));
            model.addAttribute("selectedSocietyId", effectiveSocietyId);
            String selectedSocietyName = null;
            if (effectiveSocietyId != null) {
                Society selectedSociety = allowedSocieties.get(effectiveSocietyId);
                if (selectedSociety != null) {
                    selectedSocietyName = selectedSociety.getName();
                }
            }
            model.addAttribute("selectedSocietyName", selectedSocietyName);
            return "events/form";
        }

        if (effectiveSocietyId == null) {
            ra.addFlashAttribute("error", "Please select a society.");
            return "redirect:/events/new";
        }

        if (!allowedSocieties.containsKey(effectiveSocietyId)) {
            throw new AccessDeniedException("You cannot create events for this society");
        }

        event.setSociety(societyRepo.findById(effectiveSocietyId).orElseThrow());
        event.setOrganiser(user);
        eventService.save(event);
        ra.addFlashAttribute("success", "Event created successfully.");
        return "redirect:/events?societyId=" + effectiveSocietyId;
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id,
                         Model model,
                         @AuthenticationPrincipal UserDetails currentUser) {
        Event event = eventService.findById(id);
        assertEventAccess(event, currentUser);
        model.addAttribute("event", event);
        return "events/detail";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id,
                           Model model,
                           @AuthenticationPrincipal UserDetails currentUser) {
        Event event = eventService.findById(id);
        User user = userRepo.findByUsername(currentUser.getUsername()).orElseThrow();
        requireSocietyAdmin(currentUser);

        assertEventAccess(event, currentUser);
        model.addAttribute("eventTypes", Event.EventType.values());
        model.addAttribute("societies", new ArrayList<>(getAllowedSocieties(user, false).values()));
        model.addAttribute("event", event);
        return "events/form";
    }

    @PostMapping("/{id}/edit")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("event") Event event,
                         BindingResult result,
                         @AuthenticationPrincipal UserDetails currentUser,
                         Model model,
                         RedirectAttributes ra) {
        requireSocietyAdmin(currentUser);
        Event existing = eventService.findById(id);
        assertEventAccess(existing, currentUser);

        if (result.hasErrors()) {
            User user = userRepo.findByUsername(currentUser.getUsername()).orElseThrow();
            model.addAttribute("eventTypes", Event.EventType.values());
            model.addAttribute("societies", new ArrayList<>(getAllowedSocieties(user, false).values()));
            model.addAttribute("selectedSocietyId", event.getSociety() != null ? event.getSociety().getId() : null);
            return "events/form";
        }

        eventService.update(id, event);
        ra.addFlashAttribute("success", "Event updated successfully.");
        return "redirect:/events";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id,
                         @AuthenticationPrincipal UserDetails currentUser,
                         RedirectAttributes ra) {
        try {
            requireSocietyAdmin(currentUser);
            Event event = eventService.findById(id);
            assertEventAccess(event, currentUser);
            eventService.deleteIfNoApprovedBooking(id);
            ra.addFlashAttribute("success", "Event deleted.");
        } catch (RuntimeException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/events";
    }

    private boolean hasRole(UserDetails currentUser, String role) {
        return currentUser.getAuthorities().stream().anyMatch(a -> role.equals(a.getAuthority()));
    }

    private void requireSocietyAdmin(UserDetails currentUser) {
        if (!hasRole(currentUser, "ROLE_SOCIETY_ADMIN")) {
            throw new AccessDeniedException("You are not allowed to manage events");
        }
    }

    private Map<Integer, Society> getAllowedSocieties(User user, boolean isAdmin) {
        if (isAdmin) {
            List<Society> allSocieties = societyRepo.findAll();
            Map<Integer, Society> allSocietyMap = new LinkedHashMap<>();
            for (Society society : allSocieties) {
                allSocietyMap.put(society.getId(), society);
            }
            return allSocietyMap;
        }

        List<Society> adminSocieties = societyRepo.findAllBySocietyAdminId(user.getId());
        Set<Integer> ids = new LinkedHashSet<>();
        for (Society society : adminSocieties) {
            ids.add(society.getId());
        }

        Map<Integer, Society> allowed = new LinkedHashMap<>();
        for (Integer id : ids) {
            Society society = societyRepo.findById(id).orElse(null);
            if (society != null) {
                allowed.put(id, society);
            }
        }
        return allowed;
    }

    private void assertEventAccess(Event event, UserDetails currentUser) {
        if (hasRole(currentUser, "ROLE_ADMIN")) {
            return;
        }

        User user = userRepo.findByUsername(currentUser.getUsername()).orElseThrow();
        Map<Integer, Society> allowedSocieties = getAllowedSocieties(user, false);
        Integer societyId = event.getSociety().getId();
        if (!allowedSocieties.containsKey(societyId)) {
            throw new AccessDeniedException("You cannot access this event");
        }
    }
}