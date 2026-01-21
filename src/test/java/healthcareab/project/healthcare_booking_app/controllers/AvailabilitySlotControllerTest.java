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
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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


}
