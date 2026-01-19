package healthcareab.project.healthcare_booking_app.controllers;

import healthcareab.project.healthcare_booking_app.dto.AppointmentRequest;
import healthcareab.project.healthcare_booking_app.dto.AppointmentResponse;
import healthcareab.project.healthcare_booking_app.models.*;
import healthcareab.project.healthcare_booking_app.services.AppointmentService;
import healthcareab.project.healthcare_booking_app.services.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AppointmentController Unit Tests")
public class AppointmentControllerTest {

    @Mock
    private AppointmentService appointmentService;

    @Mock
    private AuthService authService;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @Mock
    private UserDetails userDetails;

    @InjectMocks
    private AppointmentController appointmentController;

    private Patient testPatient;
    private Employee testEmployee;
    private AppointmentResponse testAppointmentResponse;
    private UUID appointmentId;
    private UUID slotId;

    @BeforeEach
    void setUp() {
        // Setup test patient
        testPatient = new Patient();
        testPatient.setUsername("patient@test.com");
        testPatient.setPassword("password");
        testPatient.setEmail("patient@test.com");
        testPatient.setFirstName("John");
        testPatient.setLastName("Doe");
        testPatient.setPhoneNumber("0701234567");
        testPatient.setDateOfBirth(LocalDate.of(1990, 1, 15));
        testPatient.setRoles(Set.of(Role.PATIENT));
        setId(testPatient, UUID.randomUUID());

        // Setup test employee
        testEmployee = new Employee();
        testEmployee.setUsername("doctor@test.com");
        testEmployee.setPassword("password");
        testEmployee.setEmail("doctor@test.com");
        testEmployee.setFirstName("Dr. Emily");
        testEmployee.setLastName("Johnson");
        testEmployee.setEmployeeNumber("EMP001");
        testEmployee.setSpecialization("Cardiology");
        testEmployee.setAvailableForBooking(true);
        testEmployee.setRoles(Set.of(Role.EMPLOYEE));
        setId(testEmployee, UUID.randomUUID());

        // Setup IDs
        appointmentId = UUID.randomUUID();
        slotId = UUID.randomUUID();

        // Setup test appointment response
        ZonedDateTime startTime = ZonedDateTime.now(ZoneId.of("UTC")).plusHours(48);
        ZonedDateTime endTime = startTime.plusMinutes(30);

        testAppointmentResponse = new AppointmentResponse(
                appointmentId,
                slotId,
                startTime,
                endTime,
                testEmployee.getId(),
                "Dr. Emily Johnson",
                "Cardiology",
                testPatient.getId(),
                "John Doe",
                AppointmentStatus.BOOKED,
                null,
                LocalDateTime.now(),
                null,
                true
        );
    }

    // Helper method to set ID using reflection
    private void setId(User user, UUID id) {
        try {
            java.lang.reflect.Field field = User.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(user, id);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set ID", e);
        }
    }

    private void setupSecurityContext(User user) {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUsername()).thenReturn(user.getUsername());
        when(authService.findByUsername(user.getUsername())).thenReturn(user);
        SecurityContextHolder.setContext(securityContext);
    }

    // ========== BOOK APPOINTMENT TESTS ==========

    @Nested
    @DisplayName("Book Appointment Tests")
    class BookAppointmentTests {

        @Test
        @DisplayName("Should successfully book an appointment")
        void bookAppointment_Success() {
            // Arrange
            setupSecurityContext(testPatient);
            AppointmentRequest request = new AppointmentRequest(slotId);

            when(appointmentService.bookAppointment(any(AppointmentRequest.class), any(User.class)))
                    .thenReturn(testAppointmentResponse);

            // Act
            ResponseEntity<AppointmentResponse> response = appointmentController.bookAppointment(request);

            // Assert
            assertEquals(HttpStatus.CREATED, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(appointmentId, response.getBody().getId());
            assertEquals(AppointmentStatus.BOOKED, response.getBody().getStatus());
            assertEquals("Dr. Emily Johnson", response.getBody().getEmployeeName());

            verify(appointmentService).bookAppointment(eq(request), eq(testPatient));
        }

        @Test
        @DisplayName("Should return error when service throws exception")
        void bookAppointment_ServiceThrows_ShouldPropagate() {
            // Arrange
            setupSecurityContext(testPatient);
            AppointmentRequest request = new AppointmentRequest(slotId);

            when(appointmentService.bookAppointment(any(AppointmentRequest.class), any(User.class)))
                    .thenThrow(new IllegalArgumentException("Slot not available"));

            // Act & Assert
            assertThrows(IllegalArgumentException.class, () ->
                    appointmentController.bookAppointment(request)
            );
        }
    }

    // ========== GET MY APPOINTMENTS TESTS ==========

    @Nested
    @DisplayName("Get My Appointments Tests")
    class GetMyAppointmentsTests {

        @Test
        @DisplayName("Should return patient appointments")
        void getMyAppointments_Success() {
            // Arrange
            setupSecurityContext(testPatient);
            List<AppointmentResponse> appointments = List.of(testAppointmentResponse);

            when(appointmentService.getPatientAppointments(any(User.class)))
                    .thenReturn(appointments);

            // Act
            ResponseEntity<List<AppointmentResponse>> response = appointmentController.getMyAppointments();

            // Assert
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(1, response.getBody().size());
            assertEquals(appointmentId, response.getBody().get(0).getId());

            verify(appointmentService).getPatientAppointments(eq(testPatient));
        }

        @Test
        @DisplayName("Should return empty list when no appointments")
        void getMyAppointments_Empty() {
            // Arrange
            setupSecurityContext(testPatient);

            when(appointmentService.getPatientAppointments(any(User.class)))
                    .thenReturn(List.of());

            // Act
            ResponseEntity<List<AppointmentResponse>> response = appointmentController.getMyAppointments();

            // Assert
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertTrue(response.getBody().isEmpty());
        }
    }

    // ========== GET EMPLOYEE APPOINTMENTS TESTS ==========

    @Nested
    @DisplayName("Get Employee Appointments Tests")
    class GetEmployeeAppointmentsTests {

        @Test
        @DisplayName("Should return employee appointments")
        void getEmployeeAppointments_Success() {
            // Arrange
            setupSecurityContext(testEmployee);
            List<AppointmentResponse> appointments = List.of(testAppointmentResponse);

            when(appointmentService.getEmployeeAppointments(any(User.class)))
                    .thenReturn(appointments);

            // Act
            ResponseEntity<List<AppointmentResponse>> response = appointmentController.getEmployeeAppointments();

            // Assert
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(1, response.getBody().size());
            assertEquals("John Doe", response.getBody().get(0).getPatientName());

            verify(appointmentService).getEmployeeAppointments(eq(testEmployee));
        }
    }

    // ========== GET APPOINTMENT BY ID TESTS ==========

    @Nested
    @DisplayName("Get Appointment By ID Tests")
    class GetAppointmentByIdTests {

        @Test
        @DisplayName("Should return appointment for patient")
        void getAppointmentById_AsPatient_Success() {
            // Arrange
            setupSecurityContext(testPatient);

            when(appointmentService.getAppointmentById(eq(appointmentId), any(User.class)))
                    .thenReturn(testAppointmentResponse);

            // Act
            ResponseEntity<AppointmentResponse> response = appointmentController.getAppointmentById(appointmentId);

            // Assert
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(appointmentId, response.getBody().getId());

            verify(appointmentService).getAppointmentById(eq(appointmentId), eq(testPatient));
        }

        @Test
        @DisplayName("Should return appointment for employee")
        void getAppointmentById_AsEmployee_Success() {
            // Arrange
            setupSecurityContext(testEmployee);

            when(appointmentService.getAppointmentById(eq(appointmentId), any(User.class)))
                    .thenReturn(testAppointmentResponse);

            // Act
            ResponseEntity<AppointmentResponse> response = appointmentController.getAppointmentById(appointmentId);

            // Assert
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());

            verify(appointmentService).getAppointmentById(eq(appointmentId), eq(testEmployee));
        }
    }

    // ========== CANCEL APPOINTMENT TESTS ==========

    @Nested
    @DisplayName("Cancel Appointment Tests")
    class CancelAppointmentTests {

        @Test
        @DisplayName("Should successfully cancel appointment by patient")
        void cancelAppointment_ByPatient_Success() {
            // Arrange
            setupSecurityContext(testPatient);

            AppointmentResponse cancelledResponse = new AppointmentResponse(
                    appointmentId,
                    slotId,
                    testAppointmentResponse.getSlotStartTime(),
                    testAppointmentResponse.getSlotEndTime(),
                    testEmployee.getId(),
                    "Dr. Emily Johnson",
                    "Cardiology",
                    testPatient.getId(),
                    "John Doe",
                    AppointmentStatus.CANCELLED,
                    null,
                    LocalDateTime.now(),
                    LocalDateTime.now(),
                    false
            );

            when(appointmentService.cancelAppointment(eq(appointmentId), any(User.class)))
                    .thenReturn(cancelledResponse);

            // Act
            ResponseEntity<Map<String, Object>> response = appointmentController.cancelAppointment(appointmentId);

            // Assert
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals("Appointment cancelled successfully", response.getBody().get("message"));
            assertNotNull(response.getBody().get("appointment"));
            assertNotNull(response.getBody().get("refundedSlot"));

            verify(appointmentService).cancelAppointment(eq(appointmentId), eq(testPatient));
        }

        @Test
        @DisplayName("Should successfully cancel appointment by employee")
        void cancelAppointment_ByEmployee_Success() {
            // Arrange
            setupSecurityContext(testEmployee);

            AppointmentResponse cancelledResponse = new AppointmentResponse(
                    appointmentId,
                    slotId,
                    testAppointmentResponse.getSlotStartTime(),
                    testAppointmentResponse.getSlotEndTime(),
                    testEmployee.getId(),
                    "Dr. Emily Johnson",
                    "Cardiology",
                    testPatient.getId(),
                    "John Doe",
                    AppointmentStatus.CANCELLED,
                    null,
                    LocalDateTime.now(),
                    LocalDateTime.now(),
                    false
            );

            when(appointmentService.cancelAppointment(eq(appointmentId), any(User.class)))
                    .thenReturn(cancelledResponse);

            // Act
            ResponseEntity<Map<String, Object>> response = appointmentController.cancelAppointment(appointmentId);

            // Assert
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals("Appointment cancelled successfully", response.getBody().get("message"));

            verify(appointmentService).cancelAppointment(eq(appointmentId), eq(testEmployee));
        }

        @Test
        @DisplayName("Should propagate exception when cancellation fails")
        void cancelAppointment_ServiceThrows_ShouldPropagate() {
            // Arrange
            setupSecurityContext(testPatient);

            when(appointmentService.cancelAppointment(eq(appointmentId), any(User.class)))
                    .thenThrow(new IllegalArgumentException("Cannot cancel less than 24 hours before"));

            // Act & Assert
            assertThrows(IllegalArgumentException.class, () ->
                    appointmentController.cancelAppointment(appointmentId)
            );
        }
    }

    // ========== AUTHENTICATION TESTS ==========

    @Nested
    @DisplayName("Authentication Tests")
    class AuthenticationTests {

        @Test
        @DisplayName("Should throw when user not authenticated")
        void anyEndpoint_NotAuthenticated_ShouldThrow() {
            // Arrange
            when(securityContext.getAuthentication()).thenReturn(null);
            SecurityContextHolder.setContext(securityContext);

            AppointmentRequest request = new AppointmentRequest(slotId);

            // Act & Assert
            assertThrows(IllegalStateException.class, () ->
                    appointmentController.bookAppointment(request)
            );
        }
    }
}

