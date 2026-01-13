package controllers;


import healthcareab.project.healthcare_booking_app.controllers.AuthController;
import healthcareab.project.healthcare_booking_app.dto.RegisterRequest;
import healthcareab.project.healthcare_booking_app.dto.RegisterResponse;
import healthcareab.project.healthcare_booking_app.factories.UserFactory;
import healthcareab.project.healthcare_booking_app.models.Patient;
import healthcareab.project.healthcare_booking_app.models.Role;
import healthcareab.project.healthcare_booking_app.models.User;
import healthcareab.project.healthcare_booking_app.services.AuthService;
import healthcareab.project.healthcare_booking_app.utils.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthController Unit Tests")
public class AuthControllerTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private AuthService authService;

    @Mock
    private UserFactory userFactory;

    @InjectMocks
    private AuthController authController;

    private RegisterRequest validRegisterRequest;
    private Patient mockPatient;

    @BeforeEach
    void setUp() {
        //Arrange: Setup test data

        validRegisterRequest = new RegisterRequest();
        validRegisterRequest.setUsername("patient@test.com");
        validRegisterRequest.setPassword("SecurePass123!");
        validRegisterRequest.setEmail("patient@test.com");
        validRegisterRequest.setFirstName("John");
        validRegisterRequest.setLastName("Doe");
        validRegisterRequest.setPhoneNumber("+46712345678");
        validRegisterRequest.setDateOfBirth(LocalDate.of(1993, 4, 14));
        validRegisterRequest.setRoles(Set.of(Role.PATIENT));

        mockPatient = new Patient();
        mockPatient.setUsername("patient@test.com");
        mockPatient.setEmail("patient@test.com");
        mockPatient.setFirstName("John");
        mockPatient.setLastName("Doe");
        mockPatient.setPhoneNumber("+46712345678");
        mockPatient.setDateOfBirth(LocalDate.of(1993, 4, 14));
        mockPatient.setRoles(Set.of(Role.PATIENT));
    }

    @Test
    @DisplayName("Should successfully register a new patient")
    void register_WithValidPatientRequest_ShouldReturnCreated() {
        // Arrange
        when(authService.existsByUsername(validRegisterRequest.getUsername())).thenReturn(false);
        when(userFactory.createUser(
                eq(Role.PATIENT),
                eq(validRegisterRequest.getUsername()),
                eq(validRegisterRequest.getPassword()),
                eq(validRegisterRequest.getEmail())
        )).thenReturn(mockPatient);
        doNothing().when(authService).registerUser(any(User.class));

        // Act
        ResponseEntity<?> response = authController.register(validRegisterRequest);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof RegisterResponse);

        RegisterResponse registerResponse = (RegisterResponse) response.getBody();
        assertEquals("User registered successfully", registerResponse.getMessage());
        assertEquals("patient@test.com", registerResponse.getUsername());
        assertEquals("John", registerResponse.getFirstName());
        assertEquals("Doe", registerResponse.getLastName());

        verify(authService, times(1)).existsByUsername(validRegisterRequest.getUsername());
        verify(userFactory, times(1)).createUser(any(Role.class), anyString(), anyString(), anyString());
        verify(authService, times(1)).registerUser(any(User.class));
    }

    @Test
    @DisplayName("Should return CONFLICT when username already exists")
    void register_WithExistingUsername_ShouldReturnConflict() {
        // Arrange
        when(authService.existsByUsername(validRegisterRequest.getUsername())).thenReturn(true);

        // Act
        ResponseEntity<?> response = authController.register(validRegisterRequest);

        // Assert
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("Username already exists.", response.getBody());

        verify(authService, times(1)).existsByUsername(validRegisterRequest.getUsername());
        verify(userFactory, never()).createUser(any(), anyString(), anyString(), anyString());
        verify(authService, never()).registerUser(any(User.class));
    }

    @Test
    @DisplayName("Should set default PATIENT role when no role is provided")
    void register_WithNoRole_ShouldUseDefaultPatientRole() {
        // Arrange
        validRegisterRequest.setRoles(null);
        when(authService.existsByUsername(anyString())).thenReturn(false);
        when(userFactory.createUser(
                eq(Role.PATIENT), // Should default to PATIENT
                anyString(),
                anyString(),
                anyString()
        )).thenReturn(mockPatient);
        doNothing().when(authService).registerUser(any(User.class));

        // Act
        ResponseEntity<?> response = authController.register(validRegisterRequest);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        verify(userFactory, times(1)).createUser(
                eq(Role.PATIENT), // Verify default role is used
                anyString(),
                anyString(),
                anyString()
        );
    }

    @Test
    @DisplayName("Should set patient-specific fields when registering a patient")
    void register_WithPatientRequest_ShouldSetPatientFields() {
        // Arrange
        when(authService.existsByUsername(anyString())).thenReturn(false);
        when(userFactory.createUser(any(), anyString(), anyString(), anyString()))
                .thenReturn(mockPatient);
        doNothing().when(authService).registerUser(any(User.class));

        // Act
        ResponseEntity<?> response = authController.register(validRegisterRequest);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        verify(authService).registerUser(argThat(user ->
                user instanceof Patient &&
                        ((Patient) user).getPhoneNumber().equals("+46712345678") &&
                        ((Patient) user).getDateOfBirth().equals(LocalDate.of(1993, 4, 14))
        ));
    }

    @Test
    @DisplayName("Should use first role from roles set when multiple roles provided")
    void register_WithMultipleRoles_ShouldUseFirstRole() {
        // Arrange
        validRegisterRequest.setRoles(Set.of(Role.EMPLOYEE, Role.ADMIN));
        when(authService.existsByUsername(anyString())).thenReturn(false);
        when(userFactory.createUser(
                eq(Role.EMPLOYEE), // Should use first role
                anyString(),
                anyString(),
                anyString()
        )).thenReturn(mockPatient);
        doNothing().when(authService).registerUser(any(User.class));

        // Act
        ResponseEntity<?> response = authController.register(validRegisterRequest);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        verify(userFactory, times(1)).createUser(
                eq(Role.EMPLOYEE),
                anyString(),
                anyString(),
                anyString()
        );
    }
}
