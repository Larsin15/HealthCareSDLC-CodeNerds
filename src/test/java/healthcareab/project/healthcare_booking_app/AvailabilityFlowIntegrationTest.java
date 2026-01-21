package healthcareab.project.healthcare_booking_app;


import healthcareab.project.healthcare_booking_app.dto.AppointmentRequest;
import healthcareab.project.healthcare_booking_app.dto.AvailabilitySlotRequest;
import healthcareab.project.healthcare_booking_app.models.*;
import healthcareab.project.healthcare_booking_app.repository.AvailabilitySlotRepository;
import healthcareab.project.healthcare_booking_app.repository.EmployeeRepository;
import healthcareab.project.healthcare_booking_app.repository.PatientRepository;
import healthcareab.project.healthcare_booking_app.services.AppointmentService;
import healthcareab.project.healthcare_booking_app.services.AvailabilitySlotService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@DisplayName("Availability & Booking Integration Tests")
public class AvailabilityFlowIntegrationTest {

    @Autowired
    private AvailabilitySlotService availabilitySlotService;

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private AvailabilitySlotRepository availabilitySlotRepository;

    private Employee createEmployee() {
        Employee e = new Employee();
        e.setUsername("doctor@test.com");
        e.setPassword("Password123!");
        e.setEmail("doctor@test.com");
        e.setFirstName("Dr");
        e.setLastName("House");
        e.setEmployeeNumber("E0001");
        e.setSpecialization("General");
        e.setDepartment("Klinik");
        e.setAvailableForBooking(true);
        e.setRoles(Set.of(Role.EMPLOYEE));
        return employeeRepository.save(e);
    }

    private Patient createPatient() {
        Patient p = new Patient();
        p.setUsername("patient@test.com");
        p.setPassword("Password123!");
        p.setEmail("patient@test.com");
        p.setFirstName("Anna");
        p.setLastName("Svensson");
        p.setPhoneNumber("0701234567");
        p.setDateOfBirth(LocalDate.of(1990, 1, 1));
        p.setRoles(Set.of(Role.PATIENT));
        return patientRepository.save(p);
    }

    private ZonedDateTime nextWeekdayAt(int hour, int minute) {
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Europe/Stockholm"));
        ZonedDateTime candidate = now.plusDays(1)
                .withHour(hour).withMinute(minute).withSecond(0).withNano(0);
        while (candidate.getDayOfWeek().getValue() >= 6) {
            candidate = candidate.plusDays(1);
        }
        return candidate;
    }


    @Nested
    @DisplayName("Complete Availability Flow")
    class CompleteAvailabilityFlow {

        @Test
        @DisplayName("Complete flow: Create slot -> list available -> book -> double book prevention")
        void completeAvailabilityAndBookingFlow() {
            // Arrange
            Employee employee = createEmployee();
            Patient patient = createPatient();

            ZonedDateTime start = nextWeekdayAt(9, 0);
            ZonedDateTime end = start.plusMinutes(30);

            AvailabilitySlotRequest slotRequest = new AvailabilitySlotRequest(start, end);

            // 1) Employee skapar availability-slot
            var slotResponse = availabilitySlotService.createSlot(slotRequest, employee);
            assertNotNull(slotResponse.getId());
            assertEquals(SlotStatus.AVAILABLE, slotResponse.getStatus());
            assertEquals(employee.getId(), slotResponse.getEmployeeId());

            // Verifiera att sloten finns i databasen
            AvailabilitySlot savedSlot = availabilitySlotRepository.findById(slotResponse.getId())
                    .orElseThrow();
            assertEquals(SlotStatus.AVAILABLE, savedSlot.getStatus());
            assertEquals(employee.getId(), savedSlot.getEmployee().getId());

            // 2) Patient ser tillgängliga slots
            var availableSlots = availabilitySlotService.getAvailableSlots(null, null);
            assertFalse(availableSlots.isEmpty());
            assertTrue(
                    availableSlots.stream().anyMatch(s -> s.getId().equals(slotResponse.getId()))
            );

            // 3) Patient bokar sloten
            AppointmentRequest appointmentRequest = new AppointmentRequest(slotResponse.getId());
            var appointmentResponse =
                    appointmentService.bookAppointment(appointmentRequest, patient);

            assertNotNull(appointmentResponse.getId());
            assertEquals(AppointmentStatus.BOOKED, appointmentResponse.getStatus());
            assertEquals(slotResponse.getId(), appointmentResponse.getAvailabilitySlotId());
            assertEquals(patient.getId(), appointmentResponse.getPatientId());
            assertEquals(employee.getId(), appointmentResponse.getEmployeeId());

            // Slotstatus ska vara BOOKED i databasen
            AvailabilitySlot bookedSlot =
                    availabilitySlotRepository.findById(slotResponse.getId()).orElseThrow();
            assertEquals(SlotStatus.BOOKED, bookedSlot.getStatus());

            // 4) Double-booking prevention: samma slot kan inte bokas igen
            IllegalArgumentException ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> appointmentService.bookAppointment(appointmentRequest, patient)
            );
            assertTrue(ex.getMessage().contains("no longer available") ||
                    ex.getMessage().contains("not available"));

            // Verifiera att sloten fortfarande är BOOKED
            AvailabilitySlot stillBooked =
                    availabilitySlotRepository.findById(slotResponse.getId()).orElseThrow();
            assertEquals(SlotStatus.BOOKED, stillBooked.getStatus());
        }



    }

}
