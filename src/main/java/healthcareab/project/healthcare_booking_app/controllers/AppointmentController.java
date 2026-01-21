package healthcareab.project.healthcare_booking_app.controllers;

import healthcareab.project.healthcare_booking_app.dto.AppointmentRequest;
import healthcareab.project.healthcare_booking_app.dto.AppointmentResponse;
import healthcareab.project.healthcare_booking_app.models.User;
import healthcareab.project.healthcare_booking_app.services.AppointmentService;
import healthcareab.project.healthcare_booking_app.services.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;
    private final AuthService authService;

    public AppointmentController(AppointmentService appointmentService, AuthService authService) {
        this.appointmentService = appointmentService;
        this.authService = authService;
    }

    /**
     * @param request the booking request containing the slot ID
     * @return the created appointment
     */
    @PostMapping
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<AppointmentResponse> bookAppointment(
            @Valid @RequestBody AppointmentRequest request) {
        User currentUser = getCurrentUser();
        AppointmentResponse response = appointmentService.bookAppointment(request, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/my-appointments")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<List<AppointmentResponse>> getMyAppointments() {
        User currentUser = getCurrentUser();
        List<AppointmentResponse> appointments = appointmentService.getPatientAppointments(currentUser);
        return ResponseEntity.ok(appointments);
    }

    @GetMapping("/employee-appointments")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<List<AppointmentResponse>> getEmployeeAppointments() {
        User currentUser = getCurrentUser();
        List<AppointmentResponse> appointments = appointmentService.getEmployeeAppointments(currentUser);
        return ResponseEntity.ok(appointments);
    }

    /**
     * Get a specific appointment by ID.
     * Both patient and employee can view their own appointments.
     * @param id the appointment ID
     * @return the appointment details
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('PATIENT', 'EMPLOYEE')")
    public ResponseEntity<AppointmentResponse> getAppointmentById(@PathVariable UUID id) {
        User currentUser = getCurrentUser();
        AppointmentResponse response = appointmentService.getAppointmentById(id, currentUser);
        return ResponseEntity.ok(response);
    }

    /**
     * Cancel an appointment.
     * Patients can cancel 24+ hours before, employees can cancel anytime.
     * @param id the appointment ID to cancel
     * @return the cancelled appointment with success message
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('PATIENT', 'EMPLOYEE')")
    public ResponseEntity<Map<String, Object>> cancelAppointment(@PathVariable UUID id) {
        User currentUser = getCurrentUser();
        AppointmentResponse response = appointmentService.cancelAppointment(id, currentUser);

        Map<String, Object> result = Map.of(
                "message", "Appointment cancelled successfully",
                "appointment", response,
                "refundedSlot", Map.of(
                        "startTime", response.getSlotStartTime(),
                        "endTime", response.getSlotEndTime()
                )
        );

        return ResponseEntity.ok(result);
    }

    // Helper method (sonar for testing purpose)
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("User not authenticated");
        }

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return authService.findByUsername(userDetails.getUsername());
    }

}

