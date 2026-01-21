package healthcareab.project.healthcare_booking_app.services;

import healthcareab.project.healthcare_booking_app.dto.AvailabilitySlotRequest;
import healthcareab.project.healthcare_booking_app.dto.AvailabilitySlotResponse;
import healthcareab.project.healthcare_booking_app.models.*;
import healthcareab.project.healthcare_booking_app.repository.AvailabilitySlotRepository;
import healthcareab.project.healthcare_booking_app.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DayOfWeek;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

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
        employee.setEmployeeNumber("E1234");
        employee.setSpecialization("General");
        employee.setAvailableForBooking(true);
        employee.setRoles(Set.of(Role.EMPLOYEE));
        employeeId = UUID.randomUUID();
        setUserId(employee, employeeId);
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

    private void setSlotId(AvailabilitySlot slot, UUID id) {
        try {
            java.lang.reflect.Field field = AvailabilitySlot.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(slot, id);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set slot ID", e);
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

    @Nested
    @DisplayName("Create slot tests")
    class CreateSlotTests {

        @Test
        @DisplayName("create a valid slot")
        void createSlot_Success() {
            ZonedDateTime start = nextWeekdayAt(9, 0);
            ZonedDateTime end = start.plusMinutes(30);

            AvailabilitySlotRequest request = new AvailabilitySlotRequest(start, end);
            AvailabilitySlot savedSlot = new AvailabilitySlot(employee, start, end);
            savedSlot.setStatus(SlotStatus.AVAILABLE);
            UUID slotId = UUID.randomUUID();
            setSlotId(savedSlot, slotId);

            when(availabilitySlotRepository.findByEmployee(employee))
                    .thenReturn(List.of());
            when(availabilitySlotRepository.save(any(AvailabilitySlot.class)))
                    .thenReturn(savedSlot);

            AvailabilitySlotResponse response =
                    availabilitySlotService.createSlot(request, employee);

            assertNotNull(response);
            assertEquals(slotId, response.getId());
            assertEquals(employeeId, response.getEmployeeId());
            assertEquals(start, response.getStartTime());
            assertEquals(end, response.getEndTime());
            assertEquals(SlotStatus.AVAILABLE, response.getStatus());

            ArgumentCaptor<AvailabilitySlot> captor =
                    ArgumentCaptor.forClass(AvailabilitySlot.class);
            verify(availabilitySlotRepository).save(captor.capture());
            assertEquals(SlotStatus.AVAILABLE, captor.getValue().getStatus());
        }

        @Test
        @DisplayName("Throw error when employee is missing role EMPLOYEE")
        void createSlot_EmployeeWithoutRole_ShouldThrow() {
            employee.setRoles(Set.of(Role.ADMIN)); // not a EMPLOYEE role

            ZonedDateTime start = nextWeekdayAt(9, 0);
            ZonedDateTime end = start.plusMinutes(30);
            AvailabilitySlotRequest request = new AvailabilitySlotRequest(start, end);

            IllegalArgumentException ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> availabilitySlotService.createSlot(request, employee)
            );
            assertTrue(ex.getMessage().contains("User must have EMPLOYEE role"));
        }


        @Test
        @DisplayName("Should throw erroe when employee isnt available for booking")
        void createSlot_EmployeeNotAvailableForBooking_ShouldThrow() {
            employee.setAvailableForBooking(false);

            ZonedDateTime start = nextWeekdayAt(9, 0);
            ZonedDateTime end = start.plusMinutes(30);
            AvailabilitySlotRequest request = new AvailabilitySlotRequest(start, end);

            IllegalArgumentException ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> availabilitySlotService.createSlot(request, employee)
            );
            assertTrue(ex.getMessage().contains("Not available for booking"));
        }







    }







}