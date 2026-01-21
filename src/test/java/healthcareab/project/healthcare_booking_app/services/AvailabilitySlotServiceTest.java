package healthcareab.project.healthcare_booking_app.services;

import healthcareab.project.healthcare_booking_app.models.Employee;
import healthcareab.project.healthcare_booking_app.models.Role;
import healthcareab.project.healthcare_booking_app.models.User;
import healthcareab.project.healthcare_booking_app.repository.AvailabilitySlotRepository;
import healthcareab.project.healthcare_booking_app.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DayOfWeek;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Set;
import java.util.UUID;

@ExtendWith(MockitoExtension.class)
@DisplayName("AvailabilitySlotService Unit Tests")
public class AvailabilitySlotServiceTest {

    @Mock
    private AvailabilitySlotRepository availabilitySlotRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private AuthService authService;

    @InjectMocks
    private AvailabilitySlotService availabilitySlotService;

    private Employee employee;
    private UUID employeeId;

    @BeforeEach
    void setUp() {
        employee = new Employee();
        employee.setUsername("employee@test.com");
        employee.setPassword("password");
        employee.setEmail("employee@test.com");
        employee.setFirstName("Anna");
        employee.setLastName("Andersson");
        employee.setEmployeeNumber("EMP02");
        employee.setSpecialization("General Nurse");
        employee.setAvailableForBooking(true);
        employee.setRoles(Set.of(Role.EMPLOYEE));
        employeeId = UUID.randomUUID();
        setUserId(employee,employeeId);
    }

    //---------------Help methods-------------------------
    private void setUserId(User user, UUID id) {
        try {
            java.lang.reflect.Field field = User.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(user, id);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set ID", e);
        }
    }

    private ZonedDateTime nextWeekdayAt(int hour, int minute) {
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Europe/Stockholm"));
        ZonedDateTime candidate = now.plusDays(1)
                .withHour(hour).withMinute(minute).withSecond(0).withNano(0);

        while (candidate.getDayOfWeek() == DayOfWeek.SATURDAY ||
                candidate.getDayOfWeek() == DayOfWeek.SUNDAY) {
            candidate = candidate.plusDays(1);
        }
        return candidate;
    }
