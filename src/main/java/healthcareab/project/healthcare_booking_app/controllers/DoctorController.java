package healthcareab.project.healthcare_booking_app.controllers;

import healthcareab.project.healthcare_booking_app.dto.AppointmentResponse;
import healthcareab.project.healthcare_booking_app.dto.UpdateAppointmentRequest;
import healthcareab.project.healthcare_booking_app.models.AppointmentStatus;
import healthcareab.project.healthcare_booking_app.models.Employee;
import healthcareab.project.healthcare_booking_app.models.User;
import healthcareab.project.healthcare_booking_app.repository.EmployeeRepository;
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

@RestController
@RequestMapping("/api/doctor")
public class DoctorController {
    private final AppointmentService appointmentService;
    private final EmployeeRepository employeeRepository;
    private final AuthService authService;

    public DoctorController(AppointmentService appointmentService,
                           EmployeeRepository employeeRepository,
                           AuthService authService) {
        this.appointmentService = appointmentService;
        this.employeeRepository = employeeRepository;
        this.authService = authService;
    }

    @PreAuthorize("hasAnyRole('DOCTOR','ADMIN')")
    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> doctorDashboard() {
        User currentUser = getCurrentUser();
        Employee employee = employeeRepository.findById(currentUser.getId())
                .orElseThrow(() -> new IllegalArgumentException("Employee not found"));

        List<AppointmentResponse> upcomingAppointments = appointmentService.getUpcomingAppointmentsByEmployee(employee.getId());
        List<AppointmentResponse> allAppointments = appointmentService.getAppointmentsByEmployee(employee.getId());

        Map<String, Object> dashboard = new HashMap<>();
        dashboard.put("employeeId", employee.getId());
        dashboard.put("employeeName", employee.getFirstName() + " " + employee.getLastName());
        dashboard.put("specialization", employee.getSpecialization());
        dashboard.put("department", employee.getDepartment());
        dashboard.put("availableForBooking", employee.isAvailableForBooking());
        dashboard.put("upcomingAppointments", upcomingAppointments);
        dashboard.put("totalAppointments", allAppointments.size());
        dashboard.put("upcomingCount", upcomingAppointments.size());

        return ResponseEntity.ok(dashboard);
    }

    @PreAuthorize("hasAnyRole('DOCTOR','ADMIN')")
    @GetMapping("/appointments")
    public ResponseEntity<List<AppointmentResponse>> getMyAppointments() {
        User currentUser = getCurrentUser();
        Employee employee = employeeRepository.findById(currentUser.getId())
                .orElseThrow(() -> new IllegalArgumentException("Employee not found"));
        return ResponseEntity.ok(appointmentService.getAppointmentsByEmployee(employee.getId()));
    }

    @PreAuthorize("hasAnyRole('DOCTOR','ADMIN')")
    @GetMapping("/appointments/upcoming")
    public ResponseEntity<List<AppointmentResponse>> getUpcomingAppointments() {
        User currentUser = getCurrentUser();
        Employee employee = employeeRepository.findById(currentUser.getId())
                .orElseThrow(() -> new IllegalArgumentException("Employee not found"));
        return ResponseEntity.ok(appointmentService.getUpcomingAppointmentsByEmployee(employee.getId()));
    }

    @PreAuthorize("hasAnyRole('DOCTOR','ADMIN')")
    @GetMapping("/appointments/{id}")
    public ResponseEntity<AppointmentResponse> getAppointmentById(@PathVariable UUID id) {
        AppointmentResponse appointment = appointmentService.getAppointmentById(id);
        User currentUser = getCurrentUser();
        Employee employee = employeeRepository.findById(currentUser.getId())
                .orElseThrow(() -> new IllegalArgumentException("Employee not found"));

        // Verify the appointment belongs to this doctor
        if (!appointment.getEmployeeId().equals(employee.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok(appointment);
    }

    @PreAuthorize("hasAnyRole('DOCTOR','ADMIN')")
    @PutMapping("/appointments/{id}/status")
    public ResponseEntity<AppointmentResponse> updateAppointmentStatus(
            @PathVariable UUID id,
            @RequestParam AppointmentStatus status) {
        try {
            User currentUser = getCurrentUser();
            boolean isAdmin = currentUser.getRoles().contains(healthcareab.project.healthcare_booking_app.models.Role.ADMIN);
            AppointmentResponse appointment = appointmentService.updateAppointmentStatus(
                    id, status, currentUser.getId(), isAdmin, true);
            return ResponseEntity.ok(appointment);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @PreAuthorize("hasAnyRole('DOCTOR','ADMIN')")
    @PutMapping("/appointments/{id}")
    public ResponseEntity<AppointmentResponse> updateAppointment(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateAppointmentRequest request) {
        try {
            User currentUser = getCurrentUser();
            Employee employee = employeeRepository.findById(currentUser.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Employee not found"));

            AppointmentResponse appointment = appointmentService.getAppointmentById(id);
            if (!appointment.getEmployeeId().equals(employee.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            // Convert UpdateAppointmentRequest to AppointmentRequest for the service
            healthcareab.project.healthcare_booking_app.dto.AppointmentRequest appointmentRequest =
                    new healthcareab.project.healthcare_booking_app.dto.AppointmentRequest();
            appointmentRequest.setAppointmentDate(request.getAppointmentDate());
            appointmentRequest.setDurationMinutes(request.getDurationMinutes() != null ? request.getDurationMinutes() : 30);
            appointmentRequest.setReason(request.getReason());
            appointmentRequest.setNotes(request.getNotes());
            appointmentRequest.setEmployeeId(employee.getId());

            boolean isAdmin = currentUser.getRoles().contains(healthcareab.project.healthcare_booking_app.models.Role.ADMIN);
            AppointmentResponse updated = appointmentService.updateAppointment(
                    id, appointmentRequest, currentUser.getId(), isAdmin);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @PreAuthorize("hasAnyRole('DOCTOR','ADMIN')")
    @PutMapping("/availability")
    public ResponseEntity<Map<String, Object>> updateAvailability(@RequestParam boolean available) {
        User currentUser = getCurrentUser();
        Employee employee = employeeRepository.findById(currentUser.getId())
                .orElseThrow(() -> new IllegalArgumentException("Employee not found"));

        employee.setAvailableForBooking(available);
        employee = employeeRepository.save(employee);

        Map<String, Object> response = new HashMap<>();
        response.put("employeeId", employee.getId());
        response.put("availableForBooking", employee.isAvailableForBooking());
        response.put("message", "Availability updated successfully");

        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyRole('DOCTOR','ADMIN')")
    @GetMapping("/profile")
    public ResponseEntity<Map<String, Object>> getMyProfile() {
        User currentUser = getCurrentUser();
        Employee employee = employeeRepository.findById(currentUser.getId())
                .orElseThrow(() -> new IllegalArgumentException("Employee not found"));

        Map<String, Object> profile = new HashMap<>();
        profile.put("id", employee.getId());
        profile.put("username", employee.getUsername());
        profile.put("email", employee.getEmail());
        profile.put("firstName", employee.getFirstName());
        profile.put("lastName", employee.getLastName());
        profile.put("address", employee.getAddress());
        profile.put("employeeNumber", employee.getEmployeeNumber());
        profile.put("specialization", employee.getSpecialization());
        profile.put("department", employee.getDepartment());
        profile.put("hireDate", employee.getHireDate());
        profile.put("availableForBooking", employee.isAvailableForBooking());
        profile.put("roles", employee.getRoles());

        return ResponseEntity.ok(profile);
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return authService.findByUsername(userDetails.getUsername());
    }
}
