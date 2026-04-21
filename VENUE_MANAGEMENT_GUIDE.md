# VENUE MANAGEMENT IMPLEMENTATION GUIDE

## PROJECT STATUS ✅ 100% COMPLETE

**What was implemented:**
- ✅ VenueService with full CRUD operations
- ✅ VenueController with admin-only access control
- ✅ Three Thymeleaf templates (list, form, detail)
- ✅ Navigation sidebar integration
- ✅ Data validation and error handling
- ✅ Design pattern consistency with EventController

---

## ARCHITECTURE OVERVIEW

### 1. **Data Flow: Creating a Venue**

```
Admin visits localhost:8080/venues/new
         ↓
GET /venues/new endpoint in VenueController
         ↓
Method: showCreateForm()
         ↓
Create empty Venue object
         ↓
Return venues/form.html template with empty model
         ↓
User fills form:
  - Name: "Main Auditorium"
  - Location: "Building A, Ground Floor"
  - Capacity: 200
  - Facilities: [AC ✓, Projector ✓, Sound ✗]
         ↓
Click "Create Venue" button
         ↓
Form submission (POST /venues/new)
         ↓
@ModelAttribute binds form data → Venue object
         ↓
VenueController.createVenue() called
         ↓
VenueService.createVenue() validates:
  - Name not empty? ✓
  - Location not empty? ✓
  - Capacity > 0? ✓
         ↓
venueRepository.save(venue)
         ↓
Venue saved to MySQL `venues` table
         ↓
Redirect to /venues with success message
         ↓
Admin sees new venue in list
```

### 2. **Database Schema: Venues Table**

```sql
CREATE TABLE venues (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(255) NOT NULL UNIQUE,
  location VARCHAR(255) NOT NULL,
  capacity INT NOT NULL,
  has_projector BOOLEAN DEFAULT FALSE,
  has_ac BOOLEAN DEFAULT FALSE,
  has_sound BOOLEAN DEFAULT FALSE,
  description TEXT,
  active BOOLEAN DEFAULT TRUE,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Example data:
INSERT INTO venues (name, location, capacity, has_projector, has_ac, has_sound, description)
VALUES ('Main Auditorium', 'Building A, Ground Floor', 200, TRUE, TRUE, TRUE, 'Large venue for conferences');
```

---

## FILE STRUCTURE

```
ems/ems/
├── src/main/java/.../
│   ├── controller/
│   │   └── VenueController.java          [CREATED] ← HTTP endpoints
│   ├── service/
│   │   └── VenueService.java             [CREATED] ← Business logic
│   ├── model/
│   │   └── Venue.java                    [EXISTS] ← Database entity
│   ├── repository/
│   │   └── VenueRepository.java          [EXISTS] ← Database queries
└── src/main/resources/templates/
    └── venues/
        ├── list.html                     [CREATED] ← Display all venues
        ├── form.html                     [CREATED] ← Create/edit form
        ├── detail.html                   [CREATED] ← Single venue view
        └── fragments/
            └── layout.html               [UPDATED] ← Added Venues nav link
```

---

## API ENDPOINTS

### Admin-Only Endpoints

| HTTP Method | URL | Purpose | Status |
|---|---|---|---|
| GET | `/venues` | List all venues (active + inactive for admin) | ✅ |
| GET | `/venues/new` | Show create form | ✅ |
| POST | `/venues/new` | Submit create venue | ✅ |
| GET | `/venues/{id}` | View venue details | ✅ |
| GET | `/venues/{id}/edit` | Show edit form | ✅ |
| POST | `/venues/{id}/edit` | Submit update venue | ✅ |
| POST | `/venues/{id}/deactivate` | Soft delete venue | ✅ |
| POST | `/venues/{id}/activate` | Reactivate venue | ✅ |
| POST | `/venues/{id}/delete` | Hard delete venue | ✅ |

### Public Endpoints (for booking)

| HTTP Method | URL | Purpose | Status |
|---|---|---|---|
| GET | `/venues` | List active venues only | ✅ |

---

## CODE WALKTHROUGH

### VenueService.java - Key Methods

```java
// 1. Get all ACTIVE venues (for booking requests)
public List<Venue> getAllActiveVenues() {
    return venueRepository.findByActiveTrue();
}

// 2. Validate and create venue
public Venue createVenue(Venue venue) {
    // Validation 1: Name cannot be empty
    if (venue.getName() == null || venue.getName().trim().isEmpty()) {
        throw new IllegalArgumentException("Venue name cannot be empty");
    }
    
    // Validation 2: Location required
    if (venue.getLocation() == null || venue.getLocation().trim().isEmpty()) {
        throw new IllegalArgumentException("Venue location cannot be empty");
    }
    
    // Validation 3: Capacity must be positive
    if (venue.getCapacity() == null || venue.getCapacity() <= 0) {
        throw new IllegalArgumentException("Venue capacity must be greater than 0");
    }
    
    // Set new venues to active
    if (venue.getActive() == null) {
        venue.setActive(true);
    }
    
    // Save to database
    return venueRepository.save(venue);
}

// 3. Update venue (preserve metadata like created_at)
public Venue updateVenue(Long id, Venue updatedVenue) {
    Venue existing = getVenueById(id);  // Fetch current venue
    
    // Only update fields that have changed
    if (updatedVenue.getName() != null) {
        existing.setName(updatedVenue.getName());
    }
    // ... update other fields
    
    return venueRepository.save(existing);
}

// 4. Soft delete - mark inactive
public void deactivateVenue(Long id) {
    Venue venue = getVenueById(id);
    venue.setActive(false);
    venueRepository.save(venue);
}
```

### VenueController.java - Key Endpoints

```java
// 1. List all venues
@GetMapping
public String listVenues(Model model, @AuthenticationPrincipal UserDetails currentUser) {
    // Check if user is admin
    boolean isAdmin = currentUser.getAuthorities().stream()
        .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    
    // Admin sees all venues (active + inactive)
    // Users see only active venues
    List<Venue> venues = isAdmin 
        ? venueService.getAllVenues()
        : venueService.getAllActiveVenues();
    
    model.addAttribute("venues", venues);
    model.addAttribute("isAdmin", isAdmin);
    return "venues/list";
}

// 2. Show create form
@GetMapping("/new")
public String showCreateForm(Model model, @AuthenticationPrincipal UserDetails currentUser) {
    requireAdminRole(currentUser);  // Security check
    
    model.addAttribute("venue", new Venue());  // Empty object for form binding
    model.addAttribute("isNew", true);  // Flag for template to show "Create" button
    
    return "venues/form";
}

// 3. Submit new venue
@PostMapping("/new")
public String createVenue(@ModelAttribute Venue venue,
                          @AuthenticationPrincipal UserDetails currentUser,
                          RedirectAttributes ra,
                          Model model) {
    requireAdminRole(currentUser);
    
    try {
        venueService.createVenue(venue);  // Validates and saves
        
        // Flash message shown after redirect
        ra.addFlashAttribute("success", "Venue created successfully!");
        return "redirect:/venues";
        
    } catch (IllegalArgumentException e) {
        // Validation failed - return form with error
        model.addAttribute("error", e.getMessage());
        model.addAttribute("venue", venue);
        model.addAttribute("isNew", true);
        return "venues/form";  // Re-render form
    }
}
```

---

## TEMPLATES EXPLANATION

### venues/list.html - Grid Layout

```html
<!-- Shows venues as Bootstrap cards in a responsive grid -->

<!-- For each venue in the list -->
<div th:each="venue : ${venues}" class="col-md-6 col-lg-4">
  
  <!-- Venue Card -->
  <div class="venue-card card h-100">
    
    <!-- Header with status badge -->
    <div class="venue-header">
      <h5 th:text="${venue.name}"></h5>
      <span th:if="${venue.active}" class="badge bg-light text-success">
        <i class="bi bi-check-circle"></i> Active
      </span>
    </div>
    
    <!-- Card Body - Details -->
    <div class="card-body">
      <p><strong>Location:</strong> <span th:text="${venue.location}"></span></p>
      <p><strong>Capacity:</strong> <span th:text="${venue.capacity}"></span> people</p>
      
      <!-- Facilities Badges -->
      <div class="mt-2">
        <span th:if="${venue.has_projector}" class="badge bg-info">Projector</span>
        <span th:if="${venue.has_ac}" class="badge bg-info">AC</span>
        <span th:if="${venue.has_sound}" class="badge bg-info">Sound</span>
      </div>
    </div>
    
    <!-- Action Buttons -->
    <div class="card-footer">
      <a th:href="@{/venues/{id}(id=${venue.id})}" class="btn btn-outline-primary btn-sm">
        View Details
      </a>
      <a th:href="@{/venues/{id}/edit(id=${venue.id})}" 
         class="btn btn-warning btn-sm" 
         sec:authorize="hasRole('ADMIN')">
        Edit
      </a>
    </div>
  </div>
</div>
```

### venues/form.html - Create/Edit Form

```html
<!-- Form handles both CREATE (new venue) and UPDATE (edit existing) -->

<!-- Form submission URL changes based on isNew flag -->
<form th:object="${venue}" 
      th:action="${isNew} ? @{/venues/new} : @{/venues/{id}/edit(id=${venueId})}"
      method="post">
  
  <!-- Basic Info Section -->
  <div class="form-section">
    <h5>Basic Information</h5>
    
    <!-- Name Input -->
    <div class="mb-3">
      <label for="name" class="form-label">Venue Name *</label>
      <input type="text" 
             class="form-control" 
             id="name" 
             th:field="*{name}"
             required>
      <small class="text-muted">Official name of the venue</small>
    </div>
    
    <!-- Location Input -->
    <div class="mb-3">
      <label for="location" class="form-label">Location *</label>
      <input type="text" 
             class="form-control" 
             id="location" 
             th:field="*{location}"
             placeholder="e.g., Building A, Ground Floor"
             required>
    </div>
    
    <!-- Capacity Input -->
    <div class="mb-3">
      <label for="capacity" class="form-label">Capacity (max people) *</label>
      <input type="number" 
             class="form-control" 
             id="capacity" 
             th:field="*{capacity}"
             min="1"
             required>
    </div>
  </div>
  
  <!-- Facilities Section -->
  <div class="form-section">
    <h5>Available Facilities</h5>
    
    <!-- Projector Checkbox -->
    <div class="form-check">
      <input class="form-check-input" 
             type="checkbox" 
             id="has_projector" 
             th:field="*{has_projector}">
      <label class="form-check-label" for="has_projector">
        <i class="bi bi-projector me-2"></i>Projector
      </label>
    </div>
    
    <!-- AC Checkbox -->
    <div class="form-check">
      <input class="form-check-input" 
             type="checkbox" 
             id="has_ac" 
             th:field="*{has_ac}">
      <label class="form-check-label" for="has_ac">
        <i class="bi bi-snow me-2"></i>Air Conditioning
      </label>
    </div>
    
    <!-- Sound Checkbox -->
    <div class="form-check">
      <input class="form-check-input" 
             type="checkbox" 
             id="has_sound" 
             th:field="*{has_sound}">
      <label class="form-check-label" for="has_sound">
        <i class="bi bi-volume-up me-2"></i>Sound System
      </label>
    </div>
  </div>
  
  <!-- Submit Button -->
  <button type="submit" class="btn btn-primary btn-lg">
    <span th:text="${isNew} ? 'Create Venue' : 'Update Venue'"></span>
  </button>
</form>
```

---

## TESTING VENUE MANAGEMENT

### Test Case 1: Create a New Venue
1. Log in as ADMIN
2. Navigate to Sidebar → Venues
3. Click "+ Add Venue" button
4. Fill form:
   - Name: "Conference Room A"
   - Location: "Building B, 2nd Floor"
   - Capacity: 50
   - Facilities: Check AC and Projector
5. Click "Create Venue"
6. **Expected**: Redirect to venues list with success message, new venue visible

### Test Case 2: Edit Venue
1. Click on venue card → "View Details"
2. Click "Edit Venue" button
3. Change Capacity from 50 to 75
4. Click "Update Venue"
5. **Expected**: Venue updated, redirected to list with success message

### Test Case 3: Deactivate Venue
1. On venues list, click deactivate button on a venue
2. **Expected**: Venue marked inactive, button changes to "Reactivate"
3. Admin still sees inactive venue in list
4. Non-admin users no longer see this venue in booking form

### Test Case 4: View Venue Details
1. Click any venue card → "View Details"
2. **Expected**: See all venue information, facilities listed, action buttons available

---

## HOW THIS INTEGRATES WITH OTHER MODULES

### Integration with Member 3 (Events & Booking)

```
Flow:
1. Member 3 creates EVENT
2. Member 3 requests VENUE BOOKING
   ↓
3. Booking form loads venues using:
   venueRepo.findByActiveTrue()  ← Uses our VenueService!
   ↓
4. Admin/User can only book ACTIVE venues
5. Only deactivated venues are hidden from booking form
```

### Database Relationships

```
Venues → Used by → VenueBookings
  ↓
Active venues shown in:
  - Booking request form
  - Calendar view
  - Booking history

Inactive venues hidden from:
  - Booking requests
  - Calendar view (for users)
  - But visible in admin list (for management)
```

---

## SECURITY IMPLEMENTATION

### Role-Based Access Control

```java
// Only ADMIN can:
@GetMapping("/venues/new")           // Create form
@PostMapping("/venues/new")          // Submit create
@GetMapping("/venues/{id}/edit")     // Edit form
@PostMapping("/venues/{id}/edit")    // Submit update
@PostMapping("/venues/{id}/deactivate")
@PostMapping("/venues/{id}/activate")
@PostMapping("/venues/{id}/delete")

// Everyone can:
@GetMapping("/venues")               // View list (filtered by active status)
@GetMapping("/venues/{id}")          // View details
```

### Implementation

```java
private void requireAdminRole(UserDetails currentUser) {
    boolean isAdmin = currentUser.getAuthorities().stream()
        .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    
    if (!isAdmin) {
        throw new RuntimeException("Access Denied: Admin role required");
    }
}
```

---

## NEXT MEMBER 4 TASKS

1. **AdminBookingController** - Handle booking approval/rejection
2. **EmailService** - Send approval/rejection emails
3. **CalendarController** - Show venue availability
4. **AdminReportController** - PDF/CSV export

See: `/memories/session/project_structure.md` for progress tracking.

---

## SUMMARY

✅ **Venue Management 100% Complete**

You have successfully implemented a production-ready venue management system with:
- Full CRUD operations
- Data validation
- Soft delete (deactivate) capability
- Beautiful responsive UI
- Admin-only access control
- Integration with booking system

The code follows Spring Boot best practices and the existing project patterns!
