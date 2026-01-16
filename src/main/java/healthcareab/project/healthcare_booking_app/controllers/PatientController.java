package healthcareab.project.healthcare_booking_app.controllers;

import healthcareab.project.healthcare_booking_app.dto.AppointmentRequest;
import healthcareab.project.healthcare_booking_app.dto.AppointmentResponse;
import healthcareab.project.healthcare_booking_app.dto.UpdateAppointmentRequest;
import healthcareab.project.healthcare_booking_app.dto.UpdateUserRequest;
import healthcareab.project.healthcare_booking_app.models.AppointmentStatus;
import healthcareab.project.healthcare_booking_app.models.Employee;
import healthcareab.project.healthcare_booking_app.models.Patient;
import healthcareab.project.healthcare_booking_app.models.User;
import healthcareab.project.healthcare_booking_app.repository.EmployeeRepository;
import healthcareab.project.healthcare_booking_app.repository.PatientRepository;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/patient")
public class PatientController {
    private final AppointmentService appointmentService;
    private final PatientRepository patientRepository;
    private final EmployeeRepository employeeRepository;
    private final AuthService authService;

    public PatientController(AppointmentService appointmentService,
                            PatientRepository patientRepository,
                            EmployeeRepository employeeRepository,
                            AuthService authService) {
        this.appointmentService = appointmentService;
        this.patientRepository = patientRepository;
        this.employeeRepository = employeeRepository;
        this.authService = authService;
    }

    @PreAuthorize("hasRole('PATIENT')")
    @GetMapping("/profile")
    public ResponseEntity<Map<String, Object>> getMyProfile() {
        User currentUser = getCurrentUser();
        Patient patient = patientRepository.findById(currentUser.getId())
                .orElseThrow(() -> new IllegalArgumentException("Patient not found"));

        Map<String, Object> profile = new HashMap<>();
        profile.put("id", patient.getId());
        profile.put("username", patient.getUsername());
        profile.put("email", patient.getEmail());
        profile.put("firstName", patient.getFirstName());
        profile.put("lastName", patient.getLastName());
        profile.put("address", patient.getAddress());
        profile.put("phoneNumber", patient.getPhoneNumber());
        profile.put("dateOfBirth", patient.getDateOfBirth());
        profile.put("roles", patient.getRoles());

        return ResponseEntity.ok(profile);
    }

    @PreAuthorize("hasRole('PATIENT')")
    @PutMapping("/profile")
    public ResponseEntity<Map<String, Object>> updateMyProfile(@Valid @RequestBody UpdateUserRequest request) {
        User currentUser = getCurrentUser();
        Patient patient = patientRepository.findById(currentUser.getId())
                .orElseThrow(() -> new IllegalArgumentException("Patient not found"));

        if (request.getEmail() != null) {
            patient.setEmail(request.getEmail());
        }
        if (request.getFirstName() != null) {
            patient.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            patient.setLastName(request.getLastName());
        }
        if (request.getAddress() != null) {
            patient.setAddress(request.getAddress());
        }
        if (request.getPhoneNumber() != null) {
            patient.setPhoneNumber(request.getPhoneNumber());
        }
        if (request.getDateOfBirth() != null) {
            patient.setDateOfBirth(request.getDateOfBirth());
        }

        patient = patientRepository.save(patient);

        Map<String, Object> profile = new HashMap<>();
        profile.put("id", patient.getId());
        profile.put("username", patient.getUsername());
        profile.put("email", patient.getEmail());
        profile.put("firstName", patient.getFirstName());
        profile.put("lastName", patient.getLastName());
        profile.put("address", patient.getAddress());
        profile.put("phoneNumber", patient.getPhoneNumber());
        profile.put("dateOfBirth", patient.getDateOfBirth());
        profile.put("message", "Profile updated successfully");

        return ResponseEntity.ok(profile);
    }

    @PreAuthorize("hasRole('PATIENT')")
    @PostMapping("/appointments")
    public ResponseEntity<AppointmentResponse> bookAppointment(@Valid @RequestBody AppointmentRequest request) {
        try {
            User currentUser = getCurrentUser();
            Patient patient = patientRepository.findById(currentUser.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Patient not found"));

            AppointmentResponse appointment = appointmentService.createAppointment(request, patient.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(appointment);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @PreAuthorize("hasRole('PATIENT')")
    @GetMapping("/appointments")
    public ResponseEntity<List<AppointmentResponse>> getMyAppointments() {
        User currentUser = getCurrentUser();
        Patient patient = patientRepository.findById(currentUser.getId())
                .orElseThrow(() -> new IllegalArgumentException("Patient not found"));
        return ResponseEntity.ok(appointmentService.getAppointmentsByPatient(patient.getId()));
    }

    @PreAuthorize("hasRole('PATIENT')")
    @GetMapping("/appointments/upcoming")
    public ResponseEntity<List<AppointmentResponse>> getUpcomingAppointments() {
        User currentUser = getCurrentUser();
        Patient patient = patientRepository.findById(currentUser.getId())
                .orElseThrow(() -> new IllegalArgumentException("Patient not found"));

        List<AppointmentResponse> allAppointments = appointmentService.getAppointmentsByPatient(patient.getId());
        List<AppointmentResponse> upcoming = allAppointments.stream()
                .filter(a -> a.getAppointmentDate().isAfter(java.time.LocalDateTime.now()) &&
                        (a.getStatus() == AppointmentStatus.SCHEDULED || a.getStatus() == AppointmentStatus.CONFIRMED))
                .collect(Collectors.toList());

        return ResponseEntity.ok(upcoming);
    }

    @PreAuthorize("hasRole('PATIENT')")
    @GetMapping("/appointments/{id}")
    public ResponseEntity<AppointmentResponse> getAppointmentById(@PathVariable UUID id) {
        AppointmentResponse appointment = appointmentService.getAppointmentById(id);
        User currentUser = getCurrentUser();

        // Verify the appointment belongs to this patient
        if (!appointment.getPatientId().equals(currentUser.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok(appointment);
    }

    @PreAuthorize("hasRole('PATIENT')")
    @PutMapping("/appointments/{id}")
    public ResponseEntity<AppointmentResponse> updateAppointment(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateAppointmentRequest request) {
        try {
            User currentUser = getCurrentUser();
            AppointmentResponse appointment = appointmentService.getAppointmentById(id);

            // Verify the appointment belongs to this patient
            if (!appointment.getPatientId().equals(currentUser.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            // Convert UpdateAppointmentRequest to AppointmentRequest for the service
            AppointmentRequest appointmentRequest = new AppointmentRequest();
            appointmentRequest.setAppointmentDate(request.getAppointmentDate());
            appointmentRequest.setDurationMinutes(request.getDurationMinutes() != null ? request.getDurationMinutes() : 30);
            appointmentRequest.setReason(request.getReason());
            appointmentRequest.setNotes(request.getNotes());
            appointmentRequest.setEmployeeId(appointment.getEmployeeId());

            AppointmentResponse updated = appointmentService.updateAppointment(
                    id, appointmentRequest, currentUser.getId(), false);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @PreAuthorize("hasRole('PATIENT')")
    @PutMapping("/appointments/{id}/cancel")
    public ResponseEntity<?> cancelAppointment(@PathVariable UUID id) {
        try {
            User currentUser = getCurrentUser();
            appointmentService.cancelAppointment(id, currentUser.getId(), false);
            return ResponseEntity.ok("Appointment cancelled successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PreAuthorize("hasRole('PATIENT')")
    @GetMapping("/doctors")
    public ResponseEntity<List<Map<String, Object>>> getAvailableDoctors() {
        List<Employee> availableEmployees = employeeRepository.findAllAvailableEmployees();
        List<Map<String, Object>> doctors = availableEmployees.stream()
                .map(employee -> {
                    Map<String, Object> doctor = new HashMap<>();
                    doctor.put("id", employee.getId());
                    doctor.put("name", employee.getFirstName() + " " + employee.getLastName());
                    doctor.put("specialization", employee.getSpecialization());
                    doctor.put("department", employee.getDepartment());
                    doctor.put("employeeNumber", employee.getEmployeeNumber());
                    return doctor;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(doctors);
    }

    @PreAuthorize("hasRole('PATIENT')")
    @GetMapping("/doctors/{specialization}")
    public ResponseEntity<List<Map<String, Object>>> getDoctorsBySpecialization(@PathVariable String specialization) {
        List<Employee> employees = employeeRepository.findAvailableEmployeesBySpecialization(specialization);
        List<Map<String, Object>> doctors = employees.stream()
                .map(employee -> {
                    Map<String, Object> doctor = new HashMap<>();
                    doctor.put("id", employee.getId());
                    doctor.put("name", employee.getFirstName() + " " + employee.getLastName());
                    doctor.put("specialization", employee.getSpecialization());
                    doctor.put("department", employee.getDepartment());
                    doctor.put("employeeNumber", employee.getEmployeeNumber());
                    return doctor;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(doctors);
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return authService.findByUsername(userDetails.getUsername());
    }
}
