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
import java.util.List;
import java.util.Set;
import java.util.UUID;

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

        @Test
        @DisplayName("Employee can create more slots and patients can see them")
        void createMultipleSlots_AllVisible() {
            Employee employee = createEmployee();
            Patient patient = createPatient();

            ZonedDateTime start1 = nextWeekdayAt(8, 0);
            ZonedDateTime end1 = start1.plusMinutes(30);
            var slot1 = availabilitySlotService.createSlot(
                    new AvailabilitySlotRequest(start1, end1), employee);

            ZonedDateTime start2 = nextWeekdayAt(10, 0);
            ZonedDateTime end2 = start2.plusMinutes(30);
            var slot2 = availabilitySlotService.createSlot(
                    new AvailabilitySlotRequest(start2, end2), employee);

            var availableSlots = availabilitySlotService.getAvailableSlots(null, null);

            assertTrue(availableSlots.size() >= 2);
            assertTrue(availableSlots.stream()
                    .anyMatch(s -> s.getId().equals(slot1.getId())));
            assertTrue(availableSlots.stream()
                    .anyMatch(s -> s.getId().equals(slot2.getId())));
        }

        @Test
        @DisplayName("getMySlots returns only the slots for logged in employees")
        void getMySlots_ReturnsOnlyOwnSlots() {
            Employee employee1 = createEmployee();
            Employee employee2 = new Employee();
            employee2.setUsername("doctor2@test.com");
            employee2.setPassword("Password123!");
            employee2.setEmail("doctor2@test.com");
            employee2.setFirstName("Dr");
            employee2.setLastName("Smith");
            employee2.setEmployeeNumber("E0002");
            employee2.setSpecialization("Cardiology");
            employee2.setAvailableForBooking(true);
            employee2.setRoles(Set.of(Role.EMPLOYEE));
            employee2 = employeeRepository.save(employee2);

            ZonedDateTime start1 = nextWeekdayAt(8, 0);
            ZonedDateTime end1 = start1.plusMinutes(30);
            availabilitySlotService.createSlot(
                    new AvailabilitySlotRequest(start1, end1), employee1);

            ZonedDateTime start2 = nextWeekdayAt(9, 0);
            ZonedDateTime end2 = start2.plusMinutes(30);
            availabilitySlotService.createSlot(
                    new AvailabilitySlotRequest(start2, end2), employee2);

            var mySlots = availabilitySlotService.getMySlots(employee1);

            assertEquals(1, mySlots.size());
            assertEquals(employee1.getId(), mySlots.get(0).getEmployeeId());
        }
    }

    @Nested
    @DisplayName("Double Booking Prevention")
    class DoubleBookingPrevention {

        @Test
        @DisplayName("Prevent overlaping availabilitySlot for the same employee")
        void preventOverlappingAvailabilitySlots() {
            Employee employee = createEmployee();

            ZonedDateTime start1 = nextWeekdayAt(8, 0);
            ZonedDateTime end1 = start1.plusMinutes(30);
            AvailabilitySlotRequest firstReq = new AvailabilitySlotRequest(start1, end1);

            // Första slot ska funka
            var first = availabilitySlotService.createSlot(firstReq, employee);
            assertNotNull(first.getId());

            // Försök skapa en överlappande slot (t.ex. 08:00-08:30 överlappar med 08:00-08:30)
            // Använd samma tid för att testa overlap
            ZonedDateTime startOverlap = start1; // Samma starttid
            ZonedDateTime endOverlap = startOverlap.plusMinutes(30);
            AvailabilitySlotRequest overlappingReq =
                    new AvailabilitySlotRequest(startOverlap, endOverlap);

            IllegalArgumentException ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> availabilitySlotService.createSlot(overlappingReq, employee)
            );
            assertTrue(ex.getMessage().contains("overlaps with existing slot"));

            // Endast en slot i databasen
            List<AvailabilitySlot> slots = availabilitySlotRepository.findByEmployee(employee);
            assertEquals(1, slots.size());
        }

        @Test
        @DisplayName("Prevent two patients from booking the same slot at the same time")
        void preventConcurrentBookingOfSameSlot() {
            Employee employee = createEmployee();
            Patient patient1 = createPatient();
            Patient patient2 = new Patient();
            patient2.setUsername("patient2@test.com");
            patient2.setPassword("Password123!");
            patient2.setEmail("patient2@test.com");
            patient2.setFirstName("Erik");
            patient2.setLastName("Johansson");
            patient2.setPhoneNumber("0707654321");
            patient2.setDateOfBirth(LocalDate.of(1985, 5, 15));
            patient2.setRoles(Set.of(Role.PATIENT));
            patient2 = patientRepository.save(patient2);

            ZonedDateTime start = nextWeekdayAt(9, 0);
            ZonedDateTime end = start.plusMinutes(30);
            var slot = availabilitySlotService.createSlot(
                    new AvailabilitySlotRequest(start, end), employee);

            // Patient 1 bokar
            AppointmentRequest request = new AppointmentRequest(slot.getId());
            var appointment1 = appointmentService.bookAppointment(request, patient1);
            assertNotNull(appointment1.getId());

            // Patient 2 försöker boka samma slot
            final UUID slotIdForPatient2 = slot.getId();
            final Patient finalPatient2 = patient2;
            IllegalArgumentException ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> appointmentService.bookAppointment(
                            new AppointmentRequest(slotIdForPatient2), finalPatient2)
            );
            assertTrue(ex.getMessage().contains("no longer available") ||
                    ex.getMessage().contains("not available"));

            // Verifiera att sloten är BOOKED
            AvailabilitySlot bookedSlot =
                    availabilitySlotRepository.findById(slot.getId()).orElseThrow();
            assertEquals(SlotStatus.BOOKED, bookedSlot.getStatus());
        }

        @Test
        @DisplayName("Prevent patients from booking an already booked slot")
        void preventBookingAlreadyBookedSlot() {
            Employee employee = createEmployee();
            Patient patient1 = createPatient();
            Patient patient2 = new Patient();
            patient2.setUsername("patient2@test.com");
            patient2.setPassword("Password123!");
            patient2.setEmail("patient2@test.com");
            patient2.setFirstName("Erik");
            patient2.setLastName("Johansson");
            patient2.setPhoneNumber("0707654321");
            patient2.setDateOfBirth(LocalDate.of(1985, 5, 15));
            patient2.setRoles(Set.of(Role.PATIENT));
            patient2 = patientRepository.save(patient2);

            ZonedDateTime start = nextWeekdayAt(10, 0);
            ZonedDateTime end = start.plusMinutes(30);
            var slot = availabilitySlotService.createSlot(
                    new AvailabilitySlotRequest(start, end), employee);

            // Patient 1 bokar
            AppointmentRequest request1 = new AppointmentRequest(slot.getId());
            appointmentService.bookAppointment(request1, patient1);

            // Patient 2 försöker boka samma slot (direkt efter)
            final UUID slotIdForPatient2 = slot.getId();
            final Patient finalPatient2 = patient2;
            IllegalArgumentException ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> appointmentService.bookAppointment(
                            new AppointmentRequest(slotIdForPatient2), finalPatient2)
            );
            assertTrue(ex.getMessage().contains("no longer available") ||
                    ex.getMessage().contains("not available"));
        }

    }

}
