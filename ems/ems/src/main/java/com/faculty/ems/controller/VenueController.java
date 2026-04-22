package com.faculty.ems.controller;
import com.faculty.ems.dto.BookingCalendarDTO;
import com.faculty.ems.model.Venue;
import com.faculty.ems.model.VenueBooking;
import com.faculty.ems.repository.VenueBookingRepository;
import com.faculty.ems.service.VenueService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.List;

@Controller
@RequestMapping("/venues")
public class VenueController {
    @Autowired
    private VenueService venueService;

    @Autowired
    private VenueBookingRepository venueBookingRepository;

    @GetMapping
    public String listVenues(Model model) {
        List<Venue> venues = venueService.getAllVenues();
        model.addAttribute("venues", venues);
        return "venues/list";
    }

    @GetMapping("/addVenue")
    public String showCreateForm(Model model) {
        model.addAttribute("venue", new Venue());
        return "venues/addVenue";
    }

    @PostMapping("/save")
    public String saveNewVenue(@Valid @ModelAttribute("venue") Venue venue,
                               BindingResult result,
                               RedirectAttributes ra,
                               Model model) {
        if (result.hasErrors()) {
            String errorMessage = result.getAllErrors().get(0).getDefaultMessage();
            model.addAttribute("message", errorMessage);
            model.addAttribute("messageType", "danger");
            model.addAttribute("status", "error");
            model.addAttribute("venue", venue);
            return "venues/addVenue";
        }
        
        venue.setActive(true);
        venueService.saveVenue(venue);
        ra.addFlashAttribute("message", "Venue added successfully!");
        ra.addFlashAttribute("messageType", "success");
        ra.addFlashAttribute("status", "success");
        return "redirect:/venues";
    }

    @GetMapping("/{id}")
    public String viewVenue(@PathVariable Long id, Model model) {
        Venue venue = venueService.getVenueById(id);
        if (venue == null) {
            return "redirect:/venues";
        }
        model.addAttribute("venue", venue);
        return "venues/view";
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model) {
        Venue venue = venueService.getVenueById(id);
        if (venue == null) {
            return "redirect:/venues";
        }
        model.addAttribute("venue", venue);
        return "venues/editVenue";
    }

    @PostMapping("/{id}/update")
    public String updateVenue(@PathVariable Long id,
                              @Valid @ModelAttribute("venue") Venue venue,
                              BindingResult result,
                              RedirectAttributes ra) {
        if (result.hasErrors()) {
            String errorMessage = result.getAllErrors().get(0).getDefaultMessage();
            ra.addFlashAttribute("message", errorMessage);
            ra.addFlashAttribute("messageType", "danger");
            ra.addFlashAttribute("status", "error");
            return "redirect:/venues/" + id + "/edit";
        }
        
        Venue existingVenue = venueService.getVenueById(id);
        if (existingVenue == null) {
            return "redirect:/venues";
        }
        
        existingVenue.setName(venue.getName());
        existingVenue.setLocation(venue.getLocation());
        existingVenue.setCapacity(venue.getCapacity());
        existingVenue.setDescription(venue.getDescription());
        existingVenue.setHas_ac(venue.getHas_ac());
        existingVenue.setHas_projector(venue.getHas_projector());
        existingVenue.setHas_sound(venue.getHas_sound());
        existingVenue.setActive(venue.getActive());
        
        venueService.saveVenue(existingVenue);
        ra.addFlashAttribute("message", "Venue updated successfully!");
        return "redirect:/venues/" + id;
    }


    @PostMapping("/{id}/delete")
    public String deleteVenue(@PathVariable Long id, RedirectAttributes ra) {
        try {
            venueService.deleteVenue(id);
            ra.addFlashAttribute("message", "Venue deleted successfully!");
            ra.addFlashAttribute("messageType", "success");
            ra.addFlashAttribute("status", "success");
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("message", e.getMessage());
            ra.addFlashAttribute("messageType", "danger");
            ra.addFlashAttribute("status", "error");
        } catch (Exception e) {
            ra.addFlashAttribute("message", "Error deleting venue: " + e.getMessage());
            ra.addFlashAttribute("messageType", "danger");
            ra.addFlashAttribute("status", "error");
        }
        return "redirect:/venues";
    }

    // ── Calendar API ───────────────────────────────────────────────
    @GetMapping("/api/bookings")
    @ResponseBody
    public List<BookingCalendarDTO> getBookingsForMonth(
            @RequestParam int month,
            @RequestParam int year) {

        List<VenueBooking> bookings = venueBookingRepository.findByMonthAndYear(month, year);

        return bookings.stream().map(b -> {
            BookingCalendarDTO dto = new BookingCalendarDTO();
            dto.setBookingId(b.getId());
            dto.setBookingDate(b.getBookingDate().toString());
            dto.setStartTime(b.getStartTime().toString());
            dto.setEndTime(b.getEndTime().toString());
            dto.setStatus(b.getStatus().name());
            dto.setEventTitle(b.getEvent().getTitle());
            dto.setEventType(b.getEvent().getEventType().name());
            dto.setEventDescription(b.getEvent().getDescription());
            dto.setVenueName(b.getVenue().getName());
            dto.setVenueLocation(b.getVenue().getLocation());
            dto.setSocietyName(b.getSociety().getName());
            return dto;
        }).toList();
    }
}
