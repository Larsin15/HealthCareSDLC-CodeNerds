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
            assertTrue(ex.getMessage().contains("not available for booking"));
        }

        @Test
        @DisplayName("Should throw error with time in the past")
        void createSlot_PastTime_ShouldThrow() {
            ZonedDateTime startPast = ZonedDateTime.now(ZoneId.of("Europe/Stockholm"))
                    .minusHours(1);
            ZonedDateTime endPast = startPast.plusMinutes(30);
            AvailabilitySlotRequest request = new AvailabilitySlotRequest(startPast, endPast);

            IllegalArgumentException ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> availabilitySlotService.createSlot(request, employee)
            );
            assertEquals("Cannot create slots in the past", ex.getMessage());
        }

        @Test
        @DisplayName("Should throw error when trying to create in weekends")
        void createSlot_Weekends_ShouldThrow() {
            ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Europe/Stockholm"));
            ZonedDateTime saturday = now.plusDays(1);
            while (saturday.getDayOfWeek() != DayOfWeek.SATURDAY) {
                saturday = saturday.plusDays(1);
            }
            saturday = saturday.withHour(9).withMinute(0).withSecond(0).withNano(0);
            ZonedDateTime end = saturday.plusMinutes(30);
            AvailabilitySlotRequest request = new AvailabilitySlotRequest(saturday, end);

            IllegalArgumentException ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> availabilitySlotService.createSlot(request, employee)
            );
            assertTrue(ex.getMessage().contains("weekdays (Monday-Friday)"));
        }

        @Test
        @DisplayName("Should throw when trying to make a slot outside working hours( before 08:00)")
        void createSlot_BeforeWorkingHours_ShouldThrow() {
            ZonedDateTime start = nextWeekdayAt(7, 0);
            ZonedDateTime end = start.plusMinutes(30);
            AvailabilitySlotRequest request = new AvailabilitySlotRequest(start, end);

            IllegalArgumentException ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> availabilitySlotService.createSlot(request, employee)
            );
            assertTrue(ex.getMessage().contains("working hours"));
        }

        @Test
        @DisplayName("Should throw error when trying to create a slot that isnt half or full hour")
        void createSlot_InvalidMinute_ShouldThrow() {
            ZonedDateTime start = nextWeekdayAt(9, 15); // 09:15
            ZonedDateTime end = start.plusMinutes(30);
            AvailabilitySlotRequest request = new AvailabilitySlotRequest(start, end);

            IllegalArgumentException ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> availabilitySlotService.createSlot(request, employee)
            );
            assertTrue(ex.getMessage().contains("Start time must be on the hour or half-hour"));
        }


        @Test
        @DisplayName("Should throw when trying to make a slot outside working hours ( after 16:00)")
        void createSlot_AfterWorkingHours_ShouldThrow() {
            ZonedDateTime start = nextWeekdayAt(16, 0);
            ZonedDateTime end = start.plusMinutes(30);
            AvailabilitySlotRequest request = new AvailabilitySlotRequest(start, end);

            IllegalArgumentException ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> availabilitySlotService.createSlot(request, employee)
            );
            assertTrue(ex.getMessage().contains("working hours"));
        }

        @Test
        @DisplayName("Should throw error when duration isnt 30 minutes")
        void createSlot_InvalidDuration_ShouldThrow() {
            ZonedDateTime start = nextWeekdayAt(9, 0);
            ZonedDateTime end = start.plusMinutes(45);
            AvailabilitySlotRequest request = new AvailabilitySlotRequest(start, end);

            IllegalArgumentException ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> availabilitySlotService.createSlot(request, employee)
            );
            assertTrue(ex.getMessage().contains("Slot duration must be exactly 30 minutes"));
        }

        @Test
        @DisplayName("Should throw an error when overlapping with exist slot")
        void createSlot_OverlappingSlot_ShouldThrow() {
            ZonedDateTime start = nextWeekdayAt(9, 0);
            ZonedDateTime end = start.plusMinutes(30);
            ZonedDateTime existingStart = start.plusMinutes(15);
            ZonedDateTime existingEnd = existingStart.plusMinutes(30);

            AvailabilitySlot existing = new AvailabilitySlot(employee, existingStart, existingEnd);
            existing.setStatus(SlotStatus.AVAILABLE);
            setSlotId(existing, UUID.randomUUID());

            when(availabilitySlotRepository.findByEmployee(employee))
                    .thenReturn(List.of(existing));

            AvailabilitySlotRequest request = new AvailabilitySlotRequest(start, end);

            IllegalArgumentException ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> availabilitySlotService.createSlot(request, employee)
            );
            assertTrue(ex.getMessage().contains("Slot overlaps with existing slot"));
        }
        @Test
        @DisplayName("Should ignore CANCELLED slot in overlap control")
        void createSlot_OverlapWithCancelledSlot_ShouldSucceed() {
            ZonedDateTime start = nextWeekdayAt(9, 0);
            ZonedDateTime end = start.plusMinutes(30);
            ZonedDateTime cancelledStart = start.plusMinutes(15);
            ZonedDateTime cancelledEnd = cancelledStart.plusMinutes(30);

            AvailabilitySlot cancelled = new AvailabilitySlot(employee, cancelledStart, cancelledEnd);
            cancelled.setStatus(SlotStatus.CANCELLED);
            setSlotId(cancelled, UUID.randomUUID());

            AvailabilitySlot savedSlot = new AvailabilitySlot(employee, start, end);
            savedSlot.setStatus(SlotStatus.AVAILABLE);
            UUID slotId = UUID.randomUUID();
            setSlotId(savedSlot, slotId);

            when(availabilitySlotRepository.findByEmployee(employee))
                    .thenReturn(List.of(cancelled));
            when(availabilitySlotRepository.save(any(AvailabilitySlot.class)))
                    .thenReturn(savedSlot);

            AvailabilitySlotRequest request = new AvailabilitySlotRequest(start, end);
            AvailabilitySlotResponse response =
                    availabilitySlotService.createSlot(request, employee);

            assertNotNull(response);
            assertEquals(slotId, response.getId());
        }
    }

    //Update slot tests

    @Nested
    @DisplayName("Update slot tests")
    class UpdateSlotTests {

        @Test
        @DisplayName("Update slot when everything is valid")
        void updateSlot_Success() {
            UUID slotId = UUID.randomUUID();
            ZonedDateTime originalStart = nextWeekdayAt(9, 0);
            ZonedDateTime originalEnd = originalStart.plusMinutes(30);

            AvailabilitySlot existing = new AvailabilitySlot(employee, originalStart, originalEnd);
            existing.setStatus(SlotStatus.AVAILABLE);
            setSlotId(existing, slotId);

            ZonedDateTime newStart = nextWeekdayAt(10, 0);
            ZonedDateTime newEnd = newStart.plusMinutes(30);
            AvailabilitySlotRequest request = new AvailabilitySlotRequest(newStart, newEnd);

            when(availabilitySlotRepository.findById(slotId))
                    .thenReturn(java.util.Optional.of(existing));
            when(availabilitySlotRepository.findByEmployee(employee))
                    .thenReturn(List.of(existing));
            when(availabilitySlotRepository.save(any(AvailabilitySlot.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            AvailabilitySlotResponse response =
                    availabilitySlotService.updateSlot(slotId, request, employee);

            assertEquals(newStart, response.getStartTime());
            assertEquals(newEnd, response.getEndTime());
        }

        @Test
        @DisplayName("Should throw error when slot doesnt exists")
        void updateSlot_SlotNotFound_ShouldThrow() {
            UUID slotId = UUID.randomUUID();
            ZonedDateTime start = nextWeekdayAt(9, 0);
            ZonedDateTime end = start.plusMinutes(30);
            AvailabilitySlotRequest request = new AvailabilitySlotRequest(start, end);

            when(availabilitySlotRepository.findById(slotId))
                    .thenReturn(java.util.Optional.empty());

            IllegalArgumentException ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> availabilitySlotService.updateSlot(slotId, request, employee)
            );
            assertEquals("Slot not found", ex.getMessage());
        }


        @Test
        @DisplayName("Should not be able to update someone elses slot")
        void updateSlot_NotOwner_ShouldThrow() {
            UUID slotId = UUID.randomUUID();
            Employee otherEmployee = new Employee();
            otherEmployee.setUsername("other@test.com");
            otherEmployee.setRoles(Set.of(Role.EMPLOYEE));
            otherEmployee.setEmployeeNumber("E9999");
            otherEmployee.setSpecialization("Other");
            otherEmployee.setAvailableForBooking(true);
            setUserId(otherEmployee, UUID.randomUUID());

            ZonedDateTime start = nextWeekdayAt(9, 0);
            ZonedDateTime end = start.plusMinutes(30);

            AvailabilitySlot existing = new AvailabilitySlot(otherEmployee, start, end);
            existing.setStatus(SlotStatus.AVAILABLE);
            setSlotId(existing, slotId);

            AvailabilitySlotRequest request = new AvailabilitySlotRequest(start, end);

            when(availabilitySlotRepository.findById(slotId))
                    .thenReturn(java.util.Optional.of(existing));

            IllegalArgumentException ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> availabilitySlotService.updateSlot(slotId, request, employee)
            );
            assertEquals("You can only update your own slots", ex.getMessage());
        }

        @Test
        @DisplayName("Should not be able to update an BOOKED slot")
        void updateSlot_Booked_ShouldThrow() {
            UUID slotId = UUID.randomUUID();
            ZonedDateTime start = nextWeekdayAt(9, 0);
            ZonedDateTime end = start.plusMinutes(30);

            AvailabilitySlot existing = new AvailabilitySlot(employee, start, end);
            existing.setStatus(SlotStatus.BOOKED);
            setSlotId(existing, slotId);

            AvailabilitySlotRequest request = new AvailabilitySlotRequest(start, end);

            when(availabilitySlotRepository.findById(slotId))
                    .thenReturn(java.util.Optional.of(existing));

            IllegalArgumentException ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> availabilitySlotService.updateSlot(slotId, request, employee)
            );
            assertTrue(ex.getMessage().contains("Cannot update BOOKED slots"));
        }

        @Test
        @DisplayName("Cannot update an COMPLETE slot")
        void updateSlot_Completed_ShouldThrow() {
            UUID slotId = UUID.randomUUID();
            ZonedDateTime start = nextWeekdayAt(9, 0);
            ZonedDateTime end = start.plusMinutes(30);

            AvailabilitySlot existing = new AvailabilitySlot(employee, start, end);
            existing.setStatus(SlotStatus.COMPLETED);
            setSlotId(existing, slotId);

            AvailabilitySlotRequest request = new AvailabilitySlotRequest(start, end);

            when(availabilitySlotRepository.findById(slotId))
                    .thenReturn(java.util.Optional.of(existing));

            IllegalArgumentException ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> availabilitySlotService.updateSlot(slotId, request, employee)
            );
            assertTrue(ex.getMessage().contains("Cannot update COMPLETED slots"));
        }



    }







}