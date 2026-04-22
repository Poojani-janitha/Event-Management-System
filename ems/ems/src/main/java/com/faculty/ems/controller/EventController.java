package com.faculty.ems.controller;

import com.faculty.ems.model.Event;
import com.faculty.ems.model.User;
import com.faculty.ems.service.EventService;
import com.faculty.ems.repository.SocietyRepository;   
import com.faculty.ems.repository.UserRepository;       
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;
    private final SocietyRepository societyRepo;
    private final UserRepository userRepo;

    @GetMapping
    public String list(Model model,
                       @AuthenticationPrincipal UserDetails currentUser) {
        User user = userRepo.findByUsername(currentUser.getUsername()).orElseThrow();

        
        if (currentUser.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            model.addAttribute("events", eventService.findAll());
        } else {
            
            var society = societyRepo.findBySocietyAdminId(user.getId()).orElseThrow();
            model.addAttribute("events", eventService.findBySociety(society.getId().longValue()));
        }
        return "events/list";
    }

    @GetMapping("/new")
    public String createForm(Model model,
                             @AuthenticationPrincipal UserDetails currentUser) {
        model.addAttribute("event", new Event());
        model.addAttribute("eventTypes", Event.EventType.values());
        model.addAttribute("societies", societyRepo.findAll());
        return "events/form";
    }

    @PostMapping("/new")
    public String create(@ModelAttribute Event event,
                         @AuthenticationPrincipal UserDetails currentUser,
                         RedirectAttributes ra) {
        User user = userRepo.findByUsername(currentUser.getUsername()).orElseThrow();
        event.setOrganiser(user);
        eventService.save(event);
        ra.addFlashAttribute("success", "Event created successfully.");
        return "redirect:/events";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        model.addAttribute("event", eventService.findById(id));
        return "events/detail";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("event", eventService.findById(id));
        model.addAttribute("eventTypes", Event.EventType.values());
        model.addAttribute("societies", societyRepo.findAll());
        return "events/form";
    }

    @PostMapping("/{id}/edit")
    public String update(@PathVariable Long id,
                         @ModelAttribute Event event,
                         RedirectAttributes ra) {
        eventService.update(id, event);
        ra.addFlashAttribute("success", "Event updated successfully.");
        return "redirect:/events";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        try {
            eventService.deleteIfNoApprovedBooking(id);
            ra.addFlashAttribute("success", "Event deleted.");
        } catch (RuntimeException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/events";
    }
}