package healthcareab.project.healthcare_booking_app.controllers;

import healthcareab.project.healthcare_booking_app.dto.AppointmentResponse;
import healthcareab.project.healthcare_booking_app.models.AppointmentStatus;
import healthcareab.project.healthcare_booking_app.models.Employee;
import healthcareab.project.healthcare_booking_app.models.User;
import healthcareab.project.healthcare_booking_app.repository.AppointmentRepository;
import healthcareab.project.healthcare_booking_app.repository.EmployeeRepository;
import healthcareab.project.healthcare_booking_app.services.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/nurse")
@PreAuthorize("hasRole('NURSE')")
public class NurseController {
    private final AppointmentRepository appointmentRepository;
    private final EmployeeRepository employeeRepository;
    private final AuthService authService;

    public NurseController(AppointmentRepository appointmentRepository,
                          EmployeeRepository employeeRepository,
                          AuthService authService) {
        this.appointmentRepository = appointmentRepository;
        this.employeeRepository = employeeRepository;
        this.authService = authService;
    }

    @GetMapping("/tasks")
    public ResponseEntity<Map<String, Object>> nurseTasks() {
        User currentUser = getCurrentUser();
        Employee employee = employeeRepository.findById(currentUser.getId())
                .orElseThrow(() -> new IllegalArgumentException("Employee not found"));

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime endOfDay = now.withHour(23).withMinute(59).withSecond(59);

        // Get today's appointments
        List<AppointmentResponse> todayAppointments = appointmentRepository
                .findByEmployeeAndDateRange(employee, now, endOfDay)
                .stream()
                .map(this::mapToAppointmentResponse)
                .collect(Collectors.toList());

        // Get upcoming appointments
        List<AppointmentResponse> upcomingAppointments = appointmentRepository
                .findByEmployeeAndDateRange(employee, now, now.plusDays(7))
                .stream()
                .filter(a -> a.getAppointmentDate().isAfter(now))
                .map(this::mapToAppointmentResponse)
                .collect(Collectors.toList());

        Map<String, Object> tasks = new HashMap<>();
        tasks.put("nurseId", employee.getId());
        tasks.put("nurseName", employee.getFirstName() + " " + employee.getLastName());
        tasks.put("department", employee.getDepartment());
        tasks.put("todayAppointments", todayAppointments);
        tasks.put("upcomingAppointments", upcomingAppointments);
        tasks.put("todayCount", todayAppointments.size());
        tasks.put("upcomingCount", upcomingAppointments.size());

        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/appointments")
    public ResponseEntity<List<AppointmentResponse>> getMyAppointments() {
        User currentUser = getCurrentUser();
        Employee employee = employeeRepository.findById(currentUser.getId())
                .orElseThrow(() -> new IllegalArgumentException("Employee not found"));

        List<AppointmentResponse> appointments = appointmentRepository.findByEmployee(employee)
                .stream()
                .map(this::mapToAppointmentResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(appointments);
    }

    @GetMapping("/appointments/today")
    public ResponseEntity<List<AppointmentResponse>> getTodayAppointments() {
        User currentUser = getCurrentUser();
        Employee employee = employeeRepository.findById(currentUser.getId())
                .orElseThrow(() -> new IllegalArgumentException("Employee not found"));

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime endOfDay = now.withHour(23).withMinute(59).withSecond(59);

        List<AppointmentResponse> appointments = appointmentRepository
                .findByEmployeeAndDateRange(employee, now, endOfDay)
                .stream()
                .map(this::mapToAppointmentResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(appointments);
    }

    @GetMapping("/appointments/{id}")
    public ResponseEntity<AppointmentResponse> getAppointmentById(@PathVariable UUID id) {
        healthcareab.project.healthcare_booking_app.models.Appointment appointment =
                appointmentRepository.findById(id)
                        .orElseThrow(() -> new IllegalArgumentException("Appointment not found"));

        User currentUser = getCurrentUser();
        Employee employee = employeeRepository.findById(currentUser.getId())
                .orElseThrow(() -> new IllegalArgumentException("Employee not found"));

        // Verify the appointment belongs to this nurse
        if (!appointment.getEmployee().getId().equals(employee.getId())) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok(mapToAppointmentResponse(appointment));
    }

    @PutMapping("/appointments/{id}/status")
    public ResponseEntity<AppointmentResponse> updateAppointmentStatus(
            @PathVariable UUID id,
            @RequestParam AppointmentStatus status) {
        healthcareab.project.healthcare_booking_app.models.Appointment appointment =
                appointmentRepository.findById(id)
                        .orElseThrow(() -> new IllegalArgumentException("Appointment not found"));

        User currentUser = getCurrentUser();
        Employee employee = employeeRepository.findById(currentUser.getId())
                .orElseThrow(() -> new IllegalArgumentException("Employee not found"));

        // Verify the appointment belongs to this nurse
        if (!appointment.getEmployee().getId().equals(employee.getId())) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.FORBIDDEN).build();
        }

        appointment.setStatus(status);
        appointment = appointmentRepository.save(appointment);

        return ResponseEntity.ok(mapToAppointmentResponse(appointment));
    }

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

    private AppointmentResponse mapToAppointmentResponse(
            healthcareab.project.healthcare_booking_app.models.Appointment appointment) {
        return new AppointmentResponse(
                appointment.getId(),
                appointment.getPatient().getId(),
                appointment.getPatient().getFirstName() + " " + appointment.getPatient().getLastName(),
                appointment.getEmployee().getId(),
                appointment.getEmployee().getFirstName() + " " + appointment.getEmployee().getLastName(),
                appointment.getEmployee().getSpecialization(),
                appointment.getAppointmentDate(),
                appointment.getDurationMinutes(),
                appointment.getStatus(),
                appointment.getReason(),
                appointment.getNotes(),
                appointment.getCreatedAt(),
                appointment.getUpdatedAt()
        );
    }
}

