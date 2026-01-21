package healthcareab.project.healthcare_booking_app.controllers;


import healthcareab.project.healthcare_booking_app.dto.AvailabilitySlotRequest;
import healthcareab.project.healthcare_booking_app.dto.AvailabilitySlotResponse;
import healthcareab.project.healthcare_booking_app.models.Employee;
import healthcareab.project.healthcare_booking_app.models.Role;
import healthcareab.project.healthcare_booking_app.models.SlotStatus;
import healthcareab.project.healthcare_booking_app.models.User;
import healthcareab.project.healthcare_booking_app.services.AuthService;
import healthcareab.project.healthcare_booking_app.services.AvailabilitySlotService;
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

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AvailabilitySlotController Unit Tests")
public class AvailabilitySlotControllerTest {

    @Mock
    private AvailabilitySlotService availabilitySlotService;

    @Mock
    private AuthService authService;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @Mock
    private UserDetails userDetails;

    @InjectMocks
    private AvailabilitySlotController availabilitySlotController;

    private Employee employee;
    private UUID employeeId;
    private UUID slotId;

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
        slotId = UUID.randomUUID();
        setUserId(employee, employeeId);
    }

    private void setUserId(User user, UUID id) {
        try {
            java.lang.reflect.Field field = User.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(user, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
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

    @Nested
    @DisplayName("Create Slot Endpoint")
    class CreateSlotEndpoint {

        @Test
        @DisplayName("Create slot and return 201 CREATED")
        void createSlot_Success() {
            setupSecurityContext(employee);

            ZonedDateTime start = ZonedDateTime.now(ZoneId.of("UTC")).plusDays(1)
                    .withHour(8).withMinute(0).withSecond(0).withNano(0);
            ZonedDateTime end = start.plusMinutes(30);
            AvailabilitySlotRequest request = new AvailabilitySlotRequest(start, end);

            AvailabilitySlotResponse responseDto = new AvailabilitySlotResponse(
                    slotId,
                    employeeId,
                    employee.getFirstName() + " " + employee.getLastName(),
                    employee.getSpecialization(),
                    start,
                    end,
                    SlotStatus.AVAILABLE
            );

            when(availabilitySlotService.createSlot(eq(request), eq(employee)))
                    .thenReturn(responseDto);

            ResponseEntity<AvailabilitySlotResponse> response =
                    availabilitySlotController.createSlot(request);

            assertEquals(HttpStatus.CREATED, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(slotId, response.getBody().getId());
            verify(availabilitySlotService).createSlot(eq(request), eq(employee));
        }
    }

    @Nested
    @DisplayName("Get My Slots Endpoint")
    class GetMySlotsEndpoint {

        @Test
        @DisplayName("Return slots for employee that is logged on")
        void getMySlots_Success() {
            setupSecurityContext(employee);

            ZonedDateTime start = ZonedDateTime.now(ZoneId.of("UTC")).plusDays(1)
                    .withHour(8).withMinute(0);
            ZonedDateTime end = start.plusMinutes(30);

            AvailabilitySlotResponse dto = new AvailabilitySlotResponse(
                    slotId,
                    employeeId,
                    employee.getFirstName() + " " + employee.getLastName(),
                    employee.getSpecialization(),
                    start,
                    end,
                    SlotStatus.AVAILABLE
            );

            when(availabilitySlotService.getMySlots(eq(employee)))
                    .thenReturn(List.of(dto));

            ResponseEntity<List<AvailabilitySlotResponse>> response =
                    availabilitySlotController.getMySlots();

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(1, response.getBody().size());
            assertEquals(slotId, response.getBody().get(0).getId());
            verify(availabilitySlotService).getMySlots(eq(employee));
        }

        @Test
        @DisplayName("Retur an empty list when employees doesnt have any slots")
        void getMySlots_Empty() {
            setupSecurityContext(employee);

            when(availabilitySlotService.getMySlots(eq(employee)))
                    .thenReturn(List.of());

            ResponseEntity<List<AvailabilitySlotResponse>> response =
                    availabilitySlotController.getMySlots();

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertTrue(response.getBody().isEmpty());
        }
    }

    @Nested
    @DisplayName("Available Slots Endpoints")
    class AvailableSlotsEndpoints {

        @Test
        @DisplayName("getAvailableSlots returns a list without filters")
        void getAvailableSlots_NoParams() {
            ZonedDateTime start = ZonedDateTime.now(ZoneId.of("UTC")).plusDays(1)
                    .withHour(8).withMinute(0);
            ZonedDateTime end = start.plusMinutes(30);

            AvailabilitySlotResponse dto = new AvailabilitySlotResponse(
                    slotId,
                    employeeId,
                    employee.getFirstName() + " " + employee.getLastName(),
                    employee.getSpecialization(),
                    start,
                    end,
                    SlotStatus.AVAILABLE
            );

            when(availabilitySlotService.getAvailableSlots(null, null))
                    .thenReturn(List.of(dto));

            ResponseEntity<List<AvailabilitySlotResponse>> response =
                    availabilitySlotController.getAvailableSlots(null, null);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(1, response.getBody().size());
            assertEquals(slotId, response.getBody().get(0).getId());
        }

        @Test
        @DisplayName("getAvailableSlots returns a list with time filters")
        void getAvailableSlots_WithTimeFilter() {
            ZonedDateTime filterStart = ZonedDateTime.now(ZoneId.of("UTC"));
            ZonedDateTime filterEnd = filterStart.plusMonths(1);
            ZonedDateTime start = filterStart.plusDays(1).withHour(8).withMinute(0);
            ZonedDateTime end = start.plusMinutes(30);

            AvailabilitySlotResponse dto = new AvailabilitySlotResponse(
                    slotId,
                    employeeId,
                    employee.getFirstName() + " " + employee.getLastName(),
                    employee.getSpecialization(),
                    start,
                    end,
                    SlotStatus.AVAILABLE
            );

            when(availabilitySlotService.getAvailableSlots(filterStart, filterEnd))
                    .thenReturn(List.of(dto));

            ResponseEntity<List<AvailabilitySlotResponse>> response =
                    availabilitySlotController.getAvailableSlots(filterStart, filterEnd);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals(1, response.getBody().size());
            verify(availabilitySlotService).getAvailableSlots(eq(filterStart), eq(filterEnd));
        }

        @Test
        @DisplayName("getAvailableSlotsByEmployee filters on employeeId")
        void getAvailableSlotsByEmployee_Success() {
            UUID requestedEmployeeId = employeeId;
            ZonedDateTime start = ZonedDateTime.now(ZoneId.of("UTC")).plusDays(1)
                    .withHour(8).withMinute(0);
            ZonedDateTime end = start.plusMinutes(30);

            AvailabilitySlotResponse dto = new AvailabilitySlotResponse(
                    slotId,
                    employeeId,
                    employee.getFirstName() + " " + employee.getLastName(),
                    employee.getSpecialization(),
                    start,
                    end,
                    SlotStatus.AVAILABLE
            );

            when(availabilitySlotService.getAvailableSlotsByEmployee(eq(requestedEmployeeId),
                    any(), any())).thenReturn(List.of(dto));

            ResponseEntity<List<AvailabilitySlotResponse>> response =
                    availabilitySlotController.getAvailableSlotsByEmployee(requestedEmployeeId, null, null);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(1, response.getBody().size());
            assertEquals(slotId, response.getBody().get(0).getId());
            verify(availabilitySlotService)
                    .getAvailableSlotsByEmployee(eq(requestedEmployeeId), isNull(), isNull());
        }

        @Test
        @DisplayName("getAvailableSlotsByEmployee with time filters")
        void getAvailableSlotsByEmployee_WithTimeFilter() {
            UUID requestedEmployeeId = employeeId;
            ZonedDateTime filterStart = ZonedDateTime.now(ZoneId.of("UTC"));
            ZonedDateTime filterEnd = filterStart.plusMonths(2);

            ZonedDateTime start = filterStart.plusDays(1).withHour(8).withMinute(0);
            ZonedDateTime end = start.plusMinutes(30);

            AvailabilitySlotResponse dto = new AvailabilitySlotResponse(
                    slotId,
                    employeeId,
                    employee.getFirstName() + " " + employee.getLastName(),
                    employee.getSpecialization(),
                    start,
                    end,
                    SlotStatus.AVAILABLE
            );

            when(availabilitySlotService.getAvailableSlotsByEmployee(
                    eq(requestedEmployeeId), eq(filterStart), eq(filterEnd)))
                    .thenReturn(List.of(dto));

            ResponseEntity<List<AvailabilitySlotResponse>> response =
                    availabilitySlotController.getAvailableSlotsByEmployee(
                            requestedEmployeeId, filterStart, filterEnd);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals(1, response.getBody().size());
            verify(availabilitySlotService).getAvailableSlotsByEmployee(
                    eq(requestedEmployeeId), eq(filterStart), eq(filterEnd));
        }
    }

    @Nested
    @DisplayName("Update & Cancel Endpoints")
    class UpdateCancelEndpoints {

        @Test
        @DisplayName("updateSlot update a slot and returns 200 OK")
        void updateSlot_Success() {
            setupSecurityContext(employee);

            ZonedDateTime start = ZonedDateTime.now(ZoneId.of("UTC")).plusDays(1)
                    .withHour(9).withMinute(0);
            ZonedDateTime end = start.plusMinutes(30);
            AvailabilitySlotRequest request = new AvailabilitySlotRequest(start, end);

            AvailabilitySlotResponse dto = new AvailabilitySlotResponse(
                    slotId,
                    employeeId,
                    employee.getFirstName() + " " + employee.getLastName(),
                    employee.getSpecialization(),
                    start,
                    end,
                    SlotStatus.AVAILABLE
            );

            when(availabilitySlotService.updateSlot(eq(slotId), eq(request), eq(employee)))
                    .thenReturn(dto);

            ResponseEntity<AvailabilitySlotResponse> response =
                    availabilitySlotController.updateSlot(slotId, request);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(slotId, response.getBody().getId());
            verify(availabilitySlotService).updateSlot(eq(slotId), eq(request), eq(employee));
        }

        @Test
        @DisplayName("updateSlot throws exception when service throws")
        void updateSlot_ServiceThrows_ShouldPropagate() {
            setupSecurityContext(employee);

            ZonedDateTime start = ZonedDateTime.now(ZoneId.of("UTC")).plusDays(1)
                    .withHour(9).withMinute(0);
            ZonedDateTime end = start.plusMinutes(30);
            AvailabilitySlotRequest request = new AvailabilitySlotRequest(start, end);

            when(availabilitySlotService.updateSlot(any(), any(), any()))
                    .thenThrow(new IllegalArgumentException("Slot not found"));

            assertThrows(IllegalArgumentException.class,
                    () -> availabilitySlotController.updateSlot(slotId, request));
        }

        @Test
        @DisplayName("cancelSlot returns 204 NO_CONTENT")
        void cancelSlot_Success() {
            setupSecurityContext(employee);

            ResponseEntity<Void> response = availabilitySlotController.cancelSlot(slotId);

            assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
            assertNull(response.getBody());
            verify(availabilitySlotService).cancelSlot(eq(slotId), eq(employee));
        }

        @Test
        @DisplayName("cancelSlot throws exception when service throws")
        void cancelSlot_ServiceThrows_ShouldPropagate() {
            setupSecurityContext(employee);

            doThrow(new IllegalArgumentException("Slot not found"))
                    .when(availabilitySlotService).cancelSlot(any(), any());

            assertThrows(IllegalArgumentException.class,
                    () -> availabilitySlotController.cancelSlot(slotId));
        }
    }


    @Nested
    @DisplayName("Authentication Tests")
    class AuthenticationTests {

        @Test
        @DisplayName("Throws when user isnt authenticated")
        void anyEndpoint_NotAuthenticated_ShouldThrow() {
            when(securityContext.getAuthentication()).thenReturn(null);
            SecurityContextHolder.setContext(securityContext);

            ZonedDateTime start = ZonedDateTime.now(ZoneId.of("UTC")).plusDays(1)
                    .withHour(8).withMinute(0);
            ZonedDateTime end = start.plusMinutes(30);
            AvailabilitySlotRequest request = new AvailabilitySlotRequest(start, end);

            assertThrows(IllegalStateException.class,
                    () -> availabilitySlotController.createSlot(request));
        }


        @Test
        @DisplayName("Throws when authentication is null")
        void anyEndpoint_NullAuthentication_ShouldThrow() {
            when(securityContext.getAuthentication()).thenReturn(null);
            SecurityContextHolder.setContext(securityContext);

            assertThrows(IllegalStateException.class,
                    () -> availabilitySlotController.getMySlots());
        }

        @Test
        @DisplayName("Throws when authentication isnt authenticated")
        void anyEndpoint_NotAuthenticatedFlag_ShouldThrow() {
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.isAuthenticated()).thenReturn(false);
            SecurityContextHolder.setContext(securityContext);

            ZonedDateTime start = ZonedDateTime.now(ZoneId.of("UTC")).plusDays(1)
                    .withHour(8).withMinute(0);
            ZonedDateTime end = start.plusMinutes(30);
            AvailabilitySlotRequest request = new AvailabilitySlotRequest(start, end);

            assertThrows(IllegalStateException.class,
                    () -> availabilitySlotController.createSlot(request));
        }
    }
}
