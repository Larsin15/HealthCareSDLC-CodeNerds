package healthcareab.project.healthcare_booking_app;


import healthcareab.project.healthcare_booking_app.models.Employee;
import healthcareab.project.healthcare_booking_app.models.Patient;
import healthcareab.project.healthcare_booking_app.models.Role;
import healthcareab.project.healthcare_booking_app.repository.AvailabilitySlotRepository;
import healthcareab.project.healthcare_booking_app.repository.EmployeeRepository;
import healthcareab.project.healthcare_booking_app.repository.PatientRepository;
import healthcareab.project.healthcare_booking_app.services.AppointmentService;
import healthcareab.project.healthcare_booking_app.services.AvailabilitySlotService;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Set;

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

}
