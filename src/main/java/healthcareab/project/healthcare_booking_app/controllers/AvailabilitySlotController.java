package healthcareab.project.healthcare_booking_app.controllers;

import healthcareab.project.healthcare_booking_app.dto.AvailabilitySlotRequest;
import healthcareab.project.healthcare_booking_app.dto.AvailabilitySlotResponse;
import healthcareab.project.healthcare_booking_app.models.User;
import healthcareab.project.healthcare_booking_app.services.AuthService;
import healthcareab.project.healthcare_booking_app.services.AvailabilitySlotService;
import jakarta.persistence.PrePersist;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/availability")
public class AvailabilitySlotController {

    private final AvailabilitySlotService availabilitySlotService;
    private final AuthService authService;

    public AvailabilitySlotController(AvailabilitySlotService availabilitySlotService, AuthService authService) {
        this.availabilitySlotService = availabilitySlotService;
        this.authService = authService;
    }

    //Create a new availability slot
    @PostMapping
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<AvailabilitySlotResponse> createSlot(
            @Valid @RequestBody AvailabilitySlotRequest request) {
        User currentUser = getCurrentUser();
        AvailabilitySlotResponse response = availabilitySlotService.createSlot(request, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    //Get all slots for the current user
    @GetMapping("/my-slots")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<List<AvailabilitySlotResponse>> getMySlots() {
        User currentUser = getCurrentUser();
        List<AvailabilitySlotResponse> slots = availabilitySlotService.getMySlots(currentUser);
        return ResponseEntity.ok(slots);
    }

    //Get all available slots for patients/Employees/admins to see
    @GetMapping("/available")
    @PreAuthorize("hasAnyRole('PATIENT', 'EMPLOYEE', 'ADMIN')")
    public ResponseEntity<List<AvailabilitySlotResponse>> getAvailableSlots(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime start,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime end) {
        List<AvailabilitySlotResponse> slots = availabilitySlotService.getAvailableSlots(start, end);
        return ResponseEntity.ok(slots);
    }

    //Get available slots for a specific emoloyee
    @GetMapping("/employee/{employeeId}")
    @PreAuthorize("hasAnyRole('PATIENT', 'EMPLOYEE', 'ADMIN')")
    public ResponseEntity<List<AvailabilitySlotResponse>> getAvailableSlotsByEmployee(
            @PathVariable UUID employeeId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime start,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime end) {
        List<AvailabilitySlotResponse> slots = availabilitySlotService
                .getAvailableSlotsByEmployee(employeeId, start, end);
        return ResponseEntity.ok(slots);
    }

    //Update an existing available slot
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<AvailabilitySlotResponse> updateSlot(
            @PathVariable UUID id,
            @Valid @RequestBody AvailabilitySlotRequest request) {
        User currentUser = getCurrentUser();
        AvailabilitySlotResponse response = availabilitySlotService.updateSlot(id, request, currentUser);
        return ResponseEntity.ok(response);
    }

    //Delete a specific a available slot
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<Void> cancelSlot(@PathVariable UUID id) {
        User currentUser = getCurrentUser();
        availabilitySlotService.cancelSlot(id, currentUser);
        return ResponseEntity.noContent().build();
    }







    //-------------------Help methods-------------------
    //Get current user
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("User not authenticated");
        }

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return authService.findByUsername(userDetails.getUsername());
    }

}
