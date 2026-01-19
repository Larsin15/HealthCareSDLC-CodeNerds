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
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
}

