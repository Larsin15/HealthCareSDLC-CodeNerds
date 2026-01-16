package healthcareab.project.healthcare_booking_app.controllers;

import healthcareab.project.healthcare_booking_app.dto.AvailabilitySlotRequest;
import healthcareab.project.healthcare_booking_app.dto.AvailabilitySlotResponse;
import healthcareab.project.healthcare_booking_app.models.User;
import healthcareab.project.healthcare_booking_app.services.AuthService;
import healthcareab.project.healthcare_booking_app.services.AvailabilitySlotService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
