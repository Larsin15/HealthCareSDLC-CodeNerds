package services;

import healthcareab.project.healthcare_booking_app.dto.AppointmentRequest;
import healthcareab.project.healthcare_booking_app.dto.AppointmentResponse;
import healthcareab.project.healthcare_booking_app.models.*;
import healthcareab.project.healthcare_booking_app.repository.AppointmentRepository;
import healthcareab.project.healthcare_booking_app.repository.AvailabilitySlotRepository;
import healthcareab.project.healthcare_booking_app.services.AppointmentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AppointmentService Unit Tests")
public class AppointmentServiceTest {

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private AvailabilitySlotRepository availabilitySlotRepository;

    @InjectMocks
    private AppointmentService appointmentService;

    private Patient testPatient;
    private Employee testEmployee;
    private AvailabilitySlot testSlot;
    private Appointment testAppointment;
    private UUID slotId;
    private UUID appointmentId;

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
        // Use reflection to set ID since there's no setter
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

        // Setup test slot - 2 hours in the future (satisfies 1 hour rule)
        slotId = UUID.randomUUID();
        ZonedDateTime startTime = ZonedDateTime.now(ZoneId.of("UTC")).plusHours(2);
        ZonedDateTime endTime = startTime.plusMinutes(30);
        testSlot = new AvailabilitySlot(testEmployee, startTime, endTime);
        testSlot.setStatus(SlotStatus.AVAILABLE);
        setSlotId(testSlot, slotId);

        // Setup test appointment
        appointmentId = UUID.randomUUID();
        testAppointment = new Appointment(testSlot, testPatient, testEmployee);
        testAppointment.setStatus(AppointmentStatus.BOOKED);
        setAppointmentId(testAppointment, appointmentId);
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

    private void setSlotId(AvailabilitySlot slot, UUID id) {
        try {
            java.lang.reflect.Field field = AvailabilitySlot.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(slot, id);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set slot ID", e);
        }
    }

    private void setAppointmentId(Appointment appointment, UUID id) {
        try {
            java.lang.reflect.Field field = Appointment.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(appointment, id);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set appointment ID", e);
        }
    }

    // ========== BOOKING TESTS ==========

    @Nested
    @DisplayName("Book Appointment Tests")
    class BookAppointmentTests {

        @Test
        @DisplayName("Should successfully book an appointment")
        void bookAppointment_Success() {
            // Arrange
            AppointmentRequest request = new AppointmentRequest(slotId);
            when(appointmentRepository.hasActiveBooking(testPatient.getId())).thenReturn(false);
            when(availabilitySlotRepository.findById(slotId)).thenReturn(Optional.of(testSlot));
            when(availabilitySlotRepository.save(any(AvailabilitySlot.class))).thenReturn(testSlot);
            when(appointmentRepository.save(any(Appointment.class))).thenReturn(testAppointment);

            // Act
            AppointmentResponse response = appointmentService.bookAppointment(request, testPatient);

            // Assert
            assertNotNull(response);
            assertEquals(AppointmentStatus.BOOKED, response.getStatus());
            assertEquals("Dr. Emily Johnson", response.getEmployeeName());
            assertEquals("Cardiology", response.getEmployeeSpecialization());

            // Verify slot status was updated
            ArgumentCaptor<AvailabilitySlot> slotCaptor = ArgumentCaptor.forClass(AvailabilitySlot.class);
            verify(availabilitySlotRepository).save(slotCaptor.capture());
            assertEquals(SlotStatus.BOOKED, slotCaptor.getValue().getStatus());

            // Verify appointment was saved
            verify(appointmentRepository).save(any(Appointment.class));
        }

        @Test
        @DisplayName("Should fail when user is not a patient")
        void bookAppointment_NotPatient_ShouldThrow() {
            // Arrange
            AppointmentRequest request = new AppointmentRequest(slotId);

            // Act & Assert
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> appointmentService.bookAppointment(request, testEmployee)
            );
            assertEquals("Only patients can book appointments", exception.getMessage());

            verify(appointmentRepository, never()).save(any(Appointment.class));
        }

        @Test
        @DisplayName("Should fail when patient already has an active booking")
        void bookAppointment_PatientHasActiveBooking_ShouldThrow() {
            // Arrange
            AppointmentRequest request = new AppointmentRequest(slotId);
            when(appointmentRepository.hasActiveBooking(testPatient.getId())).thenReturn(true);

            // Act & Assert
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> appointmentService.bookAppointment(request, testPatient)
            );
            assertTrue(exception.getMessage().contains("already have an active booking"));

            verify(appointmentRepository, never()).save(any(Appointment.class));
        }

        @Test
        @DisplayName("Should fail when slot is not found")
        void bookAppointment_SlotNotFound_ShouldThrow() {
            // Arrange
            AppointmentRequest request = new AppointmentRequest(slotId);
            when(appointmentRepository.hasActiveBooking(testPatient.getId())).thenReturn(false);
            when(availabilitySlotRepository.findById(slotId)).thenReturn(Optional.empty());

            // Act & Assert
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> appointmentService.bookAppointment(request, testPatient)
            );
            assertEquals("Availability slot not found", exception.getMessage());

            verify(appointmentRepository, never()).save(any(Appointment.class));
        }

        @Test
        @DisplayName("Should fail when slot is not available")
        void bookAppointment_SlotNotAvailable_ShouldThrow() {
            // Arrange
            testSlot.setStatus(SlotStatus.BOOKED);
            AppointmentRequest request = new AppointmentRequest(slotId);
            when(appointmentRepository.hasActiveBooking(testPatient.getId())).thenReturn(false);
            when(availabilitySlotRepository.findById(slotId)).thenReturn(Optional.of(testSlot));

            // Act & Assert
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> appointmentService.bookAppointment(request, testPatient)
            );
            assertEquals("This slot is no longer available", exception.getMessage());

            verify(appointmentRepository, never()).save(any(Appointment.class));
        }

        @Test
        @DisplayName("Should fail when booking less than 1 hour before slot")
        void bookAppointment_TooCloseToStartTime_ShouldThrow() {
            // Arrange - Set slot to start in 30 minutes (less than 1 hour)
            ZonedDateTime startTime = ZonedDateTime.now(ZoneId.of("UTC")).plusMinutes(30);
            ZonedDateTime endTime = startTime.plusMinutes(30);
            AvailabilitySlot nearSlot = new AvailabilitySlot(testEmployee, startTime, endTime);
            nearSlot.setStatus(SlotStatus.AVAILABLE);
            setSlotId(nearSlot, slotId);

            AppointmentRequest request = new AppointmentRequest(slotId);
            when(appointmentRepository.hasActiveBooking(testPatient.getId())).thenReturn(false);
            when(availabilitySlotRepository.findById(slotId)).thenReturn(Optional.of(nearSlot));

            // Act & Assert
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> appointmentService.bookAppointment(request, testPatient)
            );
            assertTrue(exception.getMessage().contains("less than 1 hour"));

            verify(appointmentRepository, never()).save(any(Appointment.class));
        }

        @Test
        @DisplayName("Should fail when employee is not available for booking")
        void bookAppointment_EmployeeNotAvailable_ShouldThrow() {
            // Arrange
            testEmployee.setAvailableForBooking(false);
            AppointmentRequest request = new AppointmentRequest(slotId);
            when(appointmentRepository.hasActiveBooking(testPatient.getId())).thenReturn(false);
            when(availabilitySlotRepository.findById(slotId)).thenReturn(Optional.of(testSlot));

            // Act & Assert
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> appointmentService.bookAppointment(request, testPatient)
            );
            assertTrue(exception.getMessage().contains("not available for booking"));

            verify(appointmentRepository, never()).save(any(Appointment.class));
        }

        @Test
        @DisplayName("Should save notes when provided")
        void bookAppointment_WithNotes_ShouldSaveNotes() {
            // Arrange
            AppointmentRequest request = new AppointmentRequest(slotId, "Please call before appointment");
            when(appointmentRepository.hasActiveBooking(testPatient.getId())).thenReturn(false);
            when(availabilitySlotRepository.findById(slotId)).thenReturn(Optional.of(testSlot));
            when(availabilitySlotRepository.save(any(AvailabilitySlot.class))).thenReturn(testSlot);
            when(appointmentRepository.save(any(Appointment.class))).thenAnswer(invocation -> {
                Appointment saved = invocation.getArgument(0);
                setAppointmentId(saved, appointmentId);
                return saved;
            });

            // Act
            appointmentService.bookAppointment(request, testPatient);

            // Assert
            ArgumentCaptor<Appointment> appointmentCaptor = ArgumentCaptor.forClass(Appointment.class);
            verify(appointmentRepository).save(appointmentCaptor.capture());
            assertEquals("Please call before appointment", appointmentCaptor.getValue().getNotes());
        }
    }

    // ========== CANCELLATION TESTS ==========

    @Nested
    @DisplayName("Cancel Appointment Tests")
    class CancelAppointmentTests {

        @Test
        @DisplayName("Should successfully cancel appointment by patient within time limit")
        void cancelAppointment_ByPatient_Success() {
            // Arrange - Slot is 48 hours in future (satisfies 24h rule)
            ZonedDateTime startTime = ZonedDateTime.now(ZoneId.of("UTC")).plusHours(48);
            ZonedDateTime endTime = startTime.plusMinutes(30);
            AvailabilitySlot futureSlot = new AvailabilitySlot(testEmployee, startTime, endTime);
            futureSlot.setStatus(SlotStatus.BOOKED);
            setSlotId(futureSlot, slotId);

            Appointment futureAppointment = new Appointment(futureSlot, testPatient, testEmployee);
            futureAppointment.setStatus(AppointmentStatus.BOOKED);
            setAppointmentId(futureAppointment, appointmentId);

            when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.of(futureAppointment));
            when(appointmentRepository.save(any(Appointment.class))).thenReturn(futureAppointment);
            when(availabilitySlotRepository.save(any(AvailabilitySlot.class))).thenReturn(futureSlot);

            // Act
            AppointmentResponse response = appointmentService.cancelAppointment(appointmentId, testPatient);

            // Assert
            assertNotNull(response);
            assertEquals(AppointmentStatus.CANCELLED, response.getStatus());

            // Verify slot status was returned to AVAILABLE
            ArgumentCaptor<AvailabilitySlot> slotCaptor = ArgumentCaptor.forClass(AvailabilitySlot.class);
            verify(availabilitySlotRepository).save(slotCaptor.capture());
            assertEquals(SlotStatus.AVAILABLE, slotCaptor.getValue().getStatus());
        }

        @Test
        @DisplayName("Should fail when patient tries to cancel less than 24 hours before")
        void cancelAppointment_ByPatient_TooLate_ShouldThrow() {
            // Arrange - Slot is only 12 hours in future (violates 24h rule)
            ZonedDateTime startTime = ZonedDateTime.now(ZoneId.of("UTC")).plusHours(12);
            ZonedDateTime endTime = startTime.plusMinutes(30);
            AvailabilitySlot nearSlot = new AvailabilitySlot(testEmployee, startTime, endTime);
            nearSlot.setStatus(SlotStatus.BOOKED);
            setSlotId(nearSlot, slotId);

            Appointment nearAppointment = new Appointment(nearSlot, testPatient, testEmployee);
            nearAppointment.setStatus(AppointmentStatus.BOOKED);
            setAppointmentId(nearAppointment, appointmentId);

            when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.of(nearAppointment));

            // Act & Assert
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> appointmentService.cancelAppointment(appointmentId, testPatient)
            );
            assertTrue(exception.getMessage().contains("24 hours"));

            verify(availabilitySlotRepository, never()).save(any(AvailabilitySlot.class));
        }

        @Test
        @DisplayName("Should allow employee to cancel anytime")
        void cancelAppointment_ByEmployee_Success() {
            // Arrange - Even if slot is only 1 hour away, employee can cancel
            ZonedDateTime startTime = ZonedDateTime.now(ZoneId.of("UTC")).plusHours(1);
            ZonedDateTime endTime = startTime.plusMinutes(30);
            AvailabilitySlot nearSlot = new AvailabilitySlot(testEmployee, startTime, endTime);
            nearSlot.setStatus(SlotStatus.BOOKED);
            setSlotId(nearSlot, slotId);

            Appointment nearAppointment = new Appointment(nearSlot, testPatient, testEmployee);
            nearAppointment.setStatus(AppointmentStatus.BOOKED);
            setAppointmentId(nearAppointment, appointmentId);

            when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.of(nearAppointment));
            when(appointmentRepository.save(any(Appointment.class))).thenReturn(nearAppointment);
            when(availabilitySlotRepository.save(any(AvailabilitySlot.class))).thenReturn(nearSlot);

            // Act
            AppointmentResponse response = appointmentService.cancelAppointment(appointmentId, testEmployee);

            // Assert
            assertNotNull(response);
            assertEquals(AppointmentStatus.CANCELLED, response.getStatus());
            verify(availabilitySlotRepository).save(any(AvailabilitySlot.class));
        }

        @Test
        @DisplayName("Should fail when appointment not found")
        void cancelAppointment_NotFound_ShouldThrow() {
            // Arrange
            when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.empty());

            // Act & Assert
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> appointmentService.cancelAppointment(appointmentId, testPatient)
            );
            assertEquals("Appointment not found", exception.getMessage());
        }

        @Test
        @DisplayName("Should fail when appointment is already cancelled")
        void cancelAppointment_AlreadyCancelled_ShouldThrow() {
            // Arrange
            testAppointment.setStatus(AppointmentStatus.CANCELLED);
            when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.of(testAppointment));

            // Act & Assert
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> appointmentService.cancelAppointment(appointmentId, testPatient)
            );
            assertTrue(exception.getMessage().contains("cannot be cancelled"));
        }

        @Test
        @DisplayName("Should fail when user is not owner of appointment")
        void cancelAppointment_NotOwner_ShouldThrow() {
            // Arrange - Create a different patient
            Patient otherPatient = new Patient();
            otherPatient.setUsername("other@test.com");
            otherPatient.setDateOfBirth(LocalDate.of(1985, 5, 10));
            setId(otherPatient, UUID.randomUUID());

            when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.of(testAppointment));

            // Act & Assert
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> appointmentService.cancelAppointment(appointmentId, otherPatient)
            );
            assertTrue(exception.getMessage().contains("only cancel your own"));
        }
    }

    // ========== GET APPOINTMENTS TESTS ==========

    @Nested
    @DisplayName("Get Appointments Tests")
    class GetAppointmentsTests {

        @Test
        @DisplayName("Should return patient appointments")
        void getPatientAppointments_Success() {
            // Arrange
            List<Appointment> appointments = List.of(testAppointment);
            when(appointmentRepository.findByPatientIdOrderBySlotStartTimeDesc(testPatient.getId()))
                    .thenReturn(appointments);

            // Act
            List<AppointmentResponse> responses = appointmentService.getPatientAppointments(testPatient);

            // Assert
            assertNotNull(responses);
            assertEquals(1, responses.size());
            assertEquals("Dr. Emily Johnson", responses.get(0).getEmployeeName());
        }

        @Test
        @DisplayName("Should return empty list when patient has no appointments")
        void getPatientAppointments_Empty() {
            // Arrange
            when(appointmentRepository.findByPatientIdOrderBySlotStartTimeDesc(testPatient.getId()))
                    .thenReturn(List.of());

            // Act
            List<AppointmentResponse> responses = appointmentService.getPatientAppointments(testPatient);

            // Assert
            assertNotNull(responses);
            assertTrue(responses.isEmpty());
        }

        @Test
        @DisplayName("Should fail when non-patient tries to get patient appointments")
        void getPatientAppointments_NotPatient_ShouldThrow() {
            // Act & Assert
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> appointmentService.getPatientAppointments(testEmployee)
            );
            assertEquals("Only patients can book appointments", exception.getMessage());
        }

        @Test
        @DisplayName("Should return employee appointments")
        void getEmployeeAppointments_Success() {
            // Arrange
            List<Appointment> appointments = List.of(testAppointment);
            when(appointmentRepository.findByEmployeeIdOrderBySlotStartTimeDesc(testEmployee.getId()))
                    .thenReturn(appointments);

            // Act
            List<AppointmentResponse> responses = appointmentService.getEmployeeAppointments(testEmployee);

            // Assert
            assertNotNull(responses);
            assertEquals(1, responses.size());
            assertEquals("John Doe", responses.get(0).getPatientName());
        }

        @Test
        @DisplayName("Should fail when non-employee tries to get employee appointments")
        void getEmployeeAppointments_NotEmployee_ShouldThrow() {
            // Act & Assert
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> appointmentService.getEmployeeAppointments(testPatient)
            );
            assertEquals("Only employees can access employee appointments", exception.getMessage());
        }
    }

    // ========== GET SINGLE APPOINTMENT TESTS ==========

    @Nested
    @DisplayName("Get Appointment By ID Tests")
    class GetAppointmentByIdTests {

        @Test
        @DisplayName("Should return appointment for patient")
        void getAppointmentById_AsPatient_Success() {
            // Arrange
            when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.of(testAppointment));

            // Act
            AppointmentResponse response = appointmentService.getAppointmentById(appointmentId, testPatient);

            // Assert
            assertNotNull(response);
            assertEquals(appointmentId, response.getId());
        }

        @Test
        @DisplayName("Should return appointment for employee")
        void getAppointmentById_AsEmployee_Success() {
            // Arrange
            when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.of(testAppointment));

            // Act
            AppointmentResponse response = appointmentService.getAppointmentById(appointmentId, testEmployee);

            // Assert
            assertNotNull(response);
            assertEquals(appointmentId, response.getId());
        }

        @Test
        @DisplayName("Should fail when appointment not found")
        void getAppointmentById_NotFound_ShouldThrow() {
            // Arrange
            when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.empty());

            // Act & Assert
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> appointmentService.getAppointmentById(appointmentId, testPatient)
            );
            assertEquals("Appointment not found", exception.getMessage());
        }

        @Test
        @DisplayName("Should fail when user is not authorized")
        void getAppointmentById_NotAuthorized_ShouldThrow() {
            // Arrange
            Patient otherPatient = new Patient();
            otherPatient.setDateOfBirth(LocalDate.of(1985, 5, 10));
            setId(otherPatient, UUID.randomUUID());

            when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.of(testAppointment));

            // Act & Assert
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> appointmentService.getAppointmentById(appointmentId, otherPatient)
            );
            assertTrue(exception.getMessage().contains("not authorized"));
        }

        @Test
        void dummyTest() {
            assertTrue(true);
        }

        @Test
        @DisplayName("Should successfully book an appointment")
        void bookAppointment_Success() {
            AppointmentRequest request = new AppointmentRequest(slotId);
            when(appointmentRepository.hasActiveBooking(testPatient.getId())).thenReturn(false);
            when(availabilitySlotRepository.findById(slotId)).thenReturn(Optional.of(testSlot));
            when(availabilitySlotRepository.save(any(AvailabilitySlot.class))).thenReturn(testSlot);
            when(appointmentRepository.save(any(Appointment.class))).thenReturn(testAppointment);

            AppointmentResponse response = appointmentService.bookAppointment(request, testPatient);

            assertNotNull(response);
            assertEquals(AppointmentStatus.BOOKED, response.getStatus());
        }


    }
}

