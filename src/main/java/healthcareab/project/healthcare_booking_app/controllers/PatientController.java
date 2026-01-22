package healthcareab.project.healthcare_booking_app.controllers;

import healthcareab.project.healthcare_booking_app.dto.PatientResponse;
import healthcareab.project.healthcare_booking_app.models.Employee;
import healthcareab.project.healthcare_booking_app.models.Patient;
import healthcareab.project.healthcare_booking_app.models.User;
import healthcareab.project.healthcare_booking_app.repository.PatientRepository;
import healthcareab.project.healthcare_booking_app.services.AppointmentService;
import healthcareab.project.healthcare_booking_app.services.AuthService;
import healthcareab.project.healthcare_booking_app.services.MaskingService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/patients")
public class PatientController {

    private final PatientRepository patientRepository;
    private final AuthService authService;
    private final AppointmentService appointmentService;
    private final MaskingService maskingService;

    public PatientController(PatientRepository patientRepository,
                             AuthService authService,
                             AppointmentService appointmentService,
                             MaskingService maskingService) {
        this.patientRepository = patientRepository;
        this.authService = authService;
        this.appointmentService = appointmentService;
        this.maskingService = maskingService;
    }

    /**
     * GET /api/patients/{id}
     * Uses masking according to GDPR.
     * Only employees with an assigned appointment see full name.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('EMPLOYEE', 'ADMIN')")  // TEMPORARILY DISABLED FOR TESTING
    public ResponseEntity<PatientResponse> getPatientById(@PathVariable UUID id) {
        Patient patient = patientRepository.findById(id)
                .orElse(null);
        if (patient == null) {
            return ResponseEntity.notFound().build();
        }

        boolean shouldMask = shouldMaskPatient(patient);
        PatientResponse response = maskingService.toPatientResponse(patient, shouldMask);
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/patients/search?firstName=&lastName=
     * Returns masked/unmasked data depending on assignment.
     */
    @GetMapping("/search")
    @PreAuthorize("hasAnyAuthority('EMPLOYEE', 'ADMIN')")// TEMPORARILY DISABLED FOR TESTING
    public ResponseEntity<List<PatientResponse>> searchPatients(
            @RequestParam(required = false) String firstName,
            @RequestParam(required = false) String lastName
    ){
        List<Patient> patients;

        if (firstName != null && lastName != null) {
            patients = patientRepository.findByFirstNameIgnoreCaseAndLastNameIgnoreCase(firstName, lastName);
        } else if (firstName != null) {
            patients = patientRepository.findByFirstNameIgnoreCase(firstName);
        } else if (lastName != null) {
            patients = patientRepository.findByLastNameIgnoreCase(lastName);
        } else {
            patients = patientRepository.findAll();
        }

        List<PatientResponse> responses = patients.stream()
                .map(p -> maskingService.toPatientResponse(p, shouldMaskPatient(p)))
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    private boolean shouldMaskPatient(Patient patient) {
        User currentUser = getCurrentUser();
        if (!(currentUser instanceof Employee)) {
            // Non-employees never see full names
            return true;
        }

        Employee employee = (Employee) currentUser;
        boolean hasAssignment = appointmentService.hasAssignedAppointment(employee, patient);

        // Mask unless employee has assigned (BOOKED/CONFIRMED) appointment
        return !hasAssignment;
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("User not authenticated");
        }

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return authService.findByUsername(userDetails.getUsername());
    }
    // Add this to PatientController temporarily for debugging
    @GetMapping("/debug/roles")
    @PreAuthorize("authenticated")
    public ResponseEntity<Map<String, Object>> debugRoles() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Map<String, Object> debug = new HashMap<>();
        debug.put("authenticated", auth != null && auth.isAuthenticated());
        debug.put("principal", auth != null ? auth.getPrincipal().getClass().getName() : null);
        debug.put("authorities", auth != null ? auth.getAuthorities().stream()
                .map(a -> a.getAuthority())
                .collect(Collectors.toList()) : null);
        return ResponseEntity.ok(debug);
    }
}