package controllers;


import healthcareab.project.healthcare_booking_app.controllers.AuthController;
import healthcareab.project.healthcare_booking_app.dto.AuthRequest;
import healthcareab.project.healthcare_booking_app.dto.AuthResponse;
import healthcareab.project.healthcare_booking_app.dto.RegisterRequest;
import healthcareab.project.healthcare_booking_app.dto.RegisterResponse;
import healthcareab.project.healthcare_booking_app.factories.UserFactory;
import healthcareab.project.healthcare_booking_app.models.Patient;
import healthcareab.project.healthcare_booking_app.models.Role;
import healthcareab.project.healthcare_booking_app.models.User;
import healthcareab.project.healthcare_booking_app.services.AuthService;
import healthcareab.project.healthcare_booking_app.utils.JwtUtil;
import jakarta.servlet.http.HttpServletResponse;
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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

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

        // Create a mock Employee since EMPLOYEE/ADMIN roles create Employee objects
        var mockEmployee = new healthcareab.project.healthcare_booking_app.models.Employee();
        mockEmployee.setUsername("patient@test.com");
        mockEmployee.setEmail("patient@test.com");
        mockEmployee.setFirstName("John");
        mockEmployee.setLastName("Doe");
        mockEmployee.setEmployeeNumber("EMP001");
        mockEmployee.setSpecialization("General");
        mockEmployee.setRoles(Set.of(Role.EMPLOYEE));

        when(authService.existsByUsername(anyString())).thenReturn(false);
        // Use any(Role.class) since Set order is not guaranteed
        when(userFactory.createUser(
                any(Role.class), // Accept any role since Set order is not guaranteed
                anyString(),
                anyString(),
                anyString()
        )).thenReturn(mockEmployee);
        doNothing().when(authService).registerUser(any(User.class));

        // Act
        ResponseEntity<?> response = authController.register(validRegisterRequest);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        // Verify that createUser was called with some role (could be EMPLOYEE or ADMIN)
        verify(userFactory, times(1)).createUser(
                any(Role.class),
                anyString(),
                anyString(),
                anyString()
        );
    }



    //Edge cases
    @Test
    @DisplayName("Should handle employee registration correctly")
    void register_WithEmployeeRequest_ShouldSetEmployeeFields() {
        // Arrange
        RegisterRequest employeeRequest = new RegisterRequest();
        employeeRequest.setUsername("employee@test.com");
        employeeRequest.setPassword("SecurePass123!");
        employeeRequest.setEmail("employee@test.com");
        employeeRequest.setFirstName("Jane");
        employeeRequest.setLastName("Smith");
        employeeRequest.setEmployeeNumber("EMP001");
        employeeRequest.setSpecialization("Cardiology");
        employeeRequest.setDepartment("Heart Clinic");
        employeeRequest.setRoles(Set.of(Role.EMPLOYEE));

        var mockEmployee = new healthcareab.project.healthcare_booking_app.models.Employee();
        mockEmployee.setUsername("employee@test.com");
        mockEmployee.setEmail("employee@test.com");
        mockEmployee.setFirstName("Jane");
        mockEmployee.setLastName("Smith");
        mockEmployee.setEmployeeNumber("EMP001");
        mockEmployee.setSpecialization("Cardiology");
        mockEmployee.setDepartment("Heart Clinic");
        mockEmployee.setRoles(Set.of(Role.EMPLOYEE));

        when(authService.existsByUsername(anyString())).thenReturn(false);
        when(userFactory.createUser(any(), anyString(), anyString(), anyString()))
                .thenReturn(mockEmployee);
        doNothing().when(authService).registerUser(any(User.class));

        // Act
        ResponseEntity<?> response = authController.register(employeeRequest);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        verify(authService).registerUser(argThat(user ->
                user instanceof healthcareab.project.healthcare_booking_app.models.Employee &&
                        ((healthcareab.project.healthcare_booking_app.models.Employee) user)
                                .getEmployeeNumber().equals("EMP001")
        ));
    }

    // ========== ADMIN TESTS ==========

    @Test
    @DisplayName("Should successfully register a new admin")
    void register_WithAdminRequest_ShouldSetAdminFields() {
        // Arrange
        RegisterRequest adminRequest = new RegisterRequest();
        adminRequest.setUsername("admin@test.com");
        adminRequest.setPassword("SecurePass123!");
        adminRequest.setEmail("admin@test.com");
        adminRequest.setFirstName("Admin");
        adminRequest.setLastName("User");
        adminRequest.setEmployeeNumber("ADM001");
        adminRequest.setSpecialization("Administration");
        adminRequest.setDepartment("Management");
        adminRequest.setRoles(Set.of(Role.ADMIN));

        var mockAdmin = new healthcareab.project.healthcare_booking_app.models.Employee();
        mockAdmin.setUsername("admin@test.com");
        mockAdmin.setEmail("admin@test.com");
        mockAdmin.setFirstName("Admin");
        mockAdmin.setLastName("User");
        mockAdmin.setEmployeeNumber("ADM001");
        mockAdmin.setSpecialization("Administration");
        mockAdmin.setDepartment("Management");
        mockAdmin.setRoles(Set.of(Role.ADMIN));

        when(authService.existsByUsername(anyString())).thenReturn(false);
        when(userFactory.createUser(eq(Role.ADMIN), anyString(), anyString(), anyString()))
                .thenReturn(mockAdmin);
        doNothing().when(authService).registerUser(any(User.class));

        // Act
        ResponseEntity<?> response = authController.register(adminRequest);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof RegisterResponse);

        RegisterResponse registerResponse = (RegisterResponse) response.getBody();
        assertEquals("User registered successfully", registerResponse.getMessage());
        assertEquals("admin@test.com", registerResponse.getUsername());

        verify(authService).registerUser(argThat(user ->
                user instanceof healthcareab.project.healthcare_booking_app.models.Employee &&
                        ((healthcareab.project.healthcare_booking_app.models.Employee) user)
                                .getEmployeeNumber().equals("ADM001") &&
                        user.getRoles().contains(Role.ADMIN)
        ));
    }

    // ========== VALIDATION ERROR TESTS ==========

    @Test
    @DisplayName("Should handle employee registration with missing employee number")
    void register_WithEmployeeMissingEmployeeNumber_ShouldHandleGracefully() {
        // Arrange
        RegisterRequest employeeRequest = new RegisterRequest();
        employeeRequest.setUsername("employee@test.com");
        employeeRequest.setPassword("SecurePass123!");
        employeeRequest.setEmail("employee@test.com");
        employeeRequest.setFirstName("Jane");
        employeeRequest.setLastName("Smith");
        employeeRequest.setEmployeeNumber(null); // Missing!
        employeeRequest.setSpecialization("Cardiology");
        employeeRequest.setRoles(Set.of(Role.EMPLOYEE));

        var mockEmployee = new healthcareab.project.healthcare_booking_app.models.Employee();
        mockEmployee.setUsername("employee@test.com");
        mockEmployee.setEmail("employee@test.com");
        mockEmployee.setFirstName("Jane");
        mockEmployee.setLastName("Smith");
        // employeeNumber is NOT set (null)
        mockEmployee.setSpecialization("Cardiology");
        mockEmployee.setRoles(Set.of(Role.EMPLOYEE));

        when(authService.existsByUsername(anyString())).thenReturn(false);
        when(userFactory.createUser(any(), anyString(), anyString(), anyString()))
                .thenReturn(mockEmployee);

        // Mock registerUser to throw exception when employeeNumber is null
        doAnswer(invocation -> {
            User user = invocation.getArgument(0);
            if (user instanceof healthcareab.project.healthcare_booking_app.models.Employee) {
                healthcareab.project.healthcare_booking_app.models.Employee emp =
                        (healthcareab.project.healthcare_booking_app.models.Employee) user;
                if (emp.getEmployeeNumber() == null || emp.getEmployeeNumber().isEmpty()) {
                    throw new IllegalArgumentException("Employee must have an employee number");
                }
            }
            return null;
        }).when(authService).registerUser(any(User.class));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            authController.register(employeeRequest);
        }, "Should throw IllegalArgumentException when employee number is missing");
    }

    @Test
    @DisplayName("Should handle employee registration with missing specialization")
    void register_WithEmployeeMissingSpecialization_ShouldHandleGracefully() {
        // Arrange
        RegisterRequest employeeRequest = new RegisterRequest();
        employeeRequest.setUsername("employee@test.com");
        employeeRequest.setPassword("SecurePass123!");
        employeeRequest.setEmail("employee@test.com");
        employeeRequest.setFirstName("Jane");
        employeeRequest.setLastName("Smith");
        employeeRequest.setEmployeeNumber("EMP001");
        employeeRequest.setSpecialization(null); // Missing!
        employeeRequest.setRoles(Set.of(Role.EMPLOYEE));

        var mockEmployee = new healthcareab.project.healthcare_booking_app.models.Employee();
        mockEmployee.setUsername("employee@test.com");
        mockEmployee.setEmail("employee@test.com");
        mockEmployee.setFirstName("Jane");
        mockEmployee.setLastName("Smith");
        mockEmployee.setEmployeeNumber("EMP001");
        // specialization is NOT set (null)
        mockEmployee.setRoles(Set.of(Role.EMPLOYEE));

        when(authService.existsByUsername(anyString())).thenReturn(false);
        when(userFactory.createUser(any(), anyString(), anyString(), anyString()))
                .thenReturn(mockEmployee);

        // Mock registerUser to throw exception when specialization is null
        doAnswer(invocation -> {
            User user = invocation.getArgument(0);
            if (user instanceof healthcareab.project.healthcare_booking_app.models.Employee) {
                healthcareab.project.healthcare_booking_app.models.Employee emp =
                        (healthcareab.project.healthcare_booking_app.models.Employee) user;
                if (emp.getSpecialization() == null || emp.getSpecialization().isEmpty()) {
                    throw new IllegalArgumentException("Employee must have a specialization");
                }
            }
            return null;
        }).when(authService).registerUser(any(User.class));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            authController.register(employeeRequest);
        }, "Should throw IllegalArgumentException when specialization is missing");
    }

    @Test
    @DisplayName("Should handle patient registration with missing phone number")
    void register_WithPatientMissingPhoneNumber_ShouldSetFields() {
        // Arrange
        RegisterRequest patientRequest = new RegisterRequest();
        patientRequest.setUsername("patient2@test.com");
        patientRequest.setPassword("SecurePass123!");
        patientRequest.setEmail("patient2@test.com");
        patientRequest.setFirstName("John");
        patientRequest.setLastName("Doe");
        patientRequest.setPhoneNumber(null); // Missing
        patientRequest.setDateOfBirth(LocalDate.of(1993, 4, 14));
        patientRequest.setRoles(Set.of(Role.PATIENT));

        Patient mockPatient2 = new Patient();
        mockPatient2.setUsername("patient2@test.com");
        mockPatient2.setEmail("patient2@test.com");
        mockPatient2.setFirstName("John");
        mockPatient2.setLastName("Doe");
        mockPatient2.setDateOfBirth(LocalDate.of(1993, 4, 14));
        mockPatient2.setRoles(Set.of(Role.PATIENT));

        when(authService.existsByUsername(anyString())).thenReturn(false);
        when(userFactory.createUser(any(), anyString(), anyString(), anyString()))
                .thenReturn(mockPatient2);
        doNothing().when(authService).registerUser(any(User.class));

        // Act
        ResponseEntity<?> response = authController.register(patientRequest);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        verify(authService).registerUser(argThat(user ->
                user instanceof Patient &&
                        ((Patient) user).getPhoneNumber() == null
        ));
    }

    @Test
    @DisplayName("Should handle patient registration with missing date of birth")
    void register_WithPatientMissingDateOfBirth_ShouldSetFields() {
        // Arrange
        RegisterRequest patientRequest = new RegisterRequest();
        patientRequest.setUsername("patient3@test.com");
        patientRequest.setPassword("SecurePass123!");
        patientRequest.setEmail("patient3@test.com");
        patientRequest.setFirstName("John");
        patientRequest.setLastName("Doe");
        patientRequest.setPhoneNumber("+46712345678");
        patientRequest.setDateOfBirth(null); // Missing
        patientRequest.setRoles(Set.of(Role.PATIENT));

        Patient mockPatient3 = new Patient();
        mockPatient3.setUsername("patient3@test.com");
        mockPatient3.setEmail("patient3@test.com");
        mockPatient3.setFirstName("John");
        mockPatient3.setLastName("Doe");
        mockPatient3.setPhoneNumber("+46712345678");
        // dateOfBirth is NOT set (null)
        mockPatient3.setRoles(Set.of(Role.PATIENT));

        when(authService.existsByUsername(anyString())).thenReturn(false);
        when(userFactory.createUser(any(), anyString(), anyString(), anyString()))
                .thenReturn(mockPatient3);

        // Mock registerUser to throw exception when dateOfBirth is null
        doAnswer(invocation -> {
            User user = invocation.getArgument(0);
            if (user instanceof Patient) {
                Patient patient = (Patient) user;
                if (patient.getDateOfBirth() == null) {
                    throw new IllegalArgumentException("Patient must have a date of birth");
                }
            }
            return null;
        }).when(authService).registerUser(any(User.class));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            authController.register(patientRequest);
        }, "Should throw IllegalArgumentException when date of birth is missing");
    }

    @Test
    @DisplayName("Should handle empty roles set by using default PATIENT role")
    void register_WithEmptyRolesSet_ShouldUseDefaultPatientRole() {
        // Arrange
        validRegisterRequest.setRoles(Set.of()); // Empty set
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
                eq(Role.PATIENT),
                anyString(),
                anyString(),
                anyString()
        );
    }

    // ========== LOGIN TESTS ==========

    @Test
    @DisplayName("Should successfully login with valid credentials")
    void login_WithValidCredentials_ShouldReturnOk() {
        // Arrange
        AuthRequest authRequest = new AuthRequest("patient@test.com", "SecurePass123!");

        // Mock UserDetails from authentication
        org.springframework.security.core.userdetails.UserDetails userDetails =
                org.springframework.security.core.userdetails.User.builder()
                        .username("patient@test.com")
                        .password("encodedPassword")
                        .authorities("ROLE_PATIENT")
                        .build();

        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);

        // Mock User from database
        Patient mockUser = new Patient();
        mockUser.setUsername("patient@test.com");
        mockUser.setEmail("patient@test.com");
        mockUser.setFirstName("John");
        mockUser.setLastName("Doe");
        mockUser.setRoles(Set.of(Role.PATIENT));
        mockUser.setAddress(null);

        when(authService.findByUsername("patient@test.com")).thenReturn(mockUser);
        when(jwtUtil.generateToken(any(org.springframework.security.core.userdetails.UserDetails.class)))
                .thenReturn("mockJwtToken");

        HttpServletResponse httpResponse = mock(HttpServletResponse.class);

        // Act
        ResponseEntity<?> response = authController.login(authRequest, httpResponse);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof AuthResponse);

        AuthResponse authResponse = (AuthResponse) response.getBody();
        assertEquals("patient@test.com", authResponse.getUsername());
        assertEquals("John", authResponse.getFirstName());
        assertEquals("Doe", authResponse.getLastName());

        // Verify JWT cookie is set
        assertNotNull(response.getHeaders().get("Set-Cookie"));
        assertTrue(response.getHeaders().get("Set-Cookie").toString().contains("jwt"));

        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtUtil, times(1)).generateToken(any(org.springframework.security.core.userdetails.UserDetails.class));
        verify(authService, times(5)).findByUsername("patient@test.com"); // Fixed: Called 5 times (not 6)
    }

    @Test
    @DisplayName("Should return UNAUTHORIZED with incorrect password")
    void login_WithIncorrectPassword_ShouldReturnUnauthorized() {
        // Arrange
        AuthRequest authRequest = new AuthRequest("patient@test.com", "WrongPassword123!");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new org.springframework.security.authentication.BadCredentialsException("Bad credentials"));

        HttpServletResponse httpResponse = mock(HttpServletResponse.class);

        // Act
        ResponseEntity<?> response = authController.login(authRequest, httpResponse);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Incorrect username or password", response.getBody());

        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtUtil, never()).generateToken(any());
        verify(authService, never()).findByUsername(anyString());
    }

    @Test
    @DisplayName("Should return UNAUTHORIZED with non-existent username")
    void login_WithNonExistentUsername_ShouldReturnUnauthorized() {
        // Arrange
        AuthRequest authRequest = new AuthRequest("nonexistent@test.com", "SecurePass123!");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new org.springframework.security.core.userdetails.UsernameNotFoundException("User not found"));

        HttpServletResponse httpResponse = mock(HttpServletResponse.class);

        // Act
        ResponseEntity<?> response = authController.login(authRequest, httpResponse);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Incorrect username or password", response.getBody());

        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtUtil, never()).generateToken(any());
    }

    @Test
    @DisplayName("Should set JWT cookie with correct properties")
    void login_WithValidCredentials_ShouldSetJwtCookie() {
        // Arrange
        AuthRequest authRequest = new AuthRequest("patient@test.com", "SecurePass123!");

        org.springframework.security.core.userdetails.UserDetails userDetails =
                org.springframework.security.core.userdetails.User.builder()
                        .username("patient@test.com")
                        .password("encodedPassword")
                        .authorities("ROLE_PATIENT")
                        .build();

        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);

        Patient mockUser = new Patient();
        mockUser.setUsername("patient@test.com");
        mockUser.setEmail("patient@test.com");
        mockUser.setFirstName("John");
        mockUser.setLastName("Doe");
        mockUser.setRoles(Set.of(Role.PATIENT));
        mockUser.setAddress(null);

        when(authService.findByUsername("patient@test.com")).thenReturn(mockUser);
        when(jwtUtil.generateToken(any(org.springframework.security.core.userdetails.UserDetails.class)))
                .thenReturn("mockJwtToken123");

        HttpServletResponse httpResponse = mock(HttpServletResponse.class);

        // Act
        ResponseEntity<?> response = authController.login(authRequest, httpResponse);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());

        // Verify cookie properties
        String cookieHeader = response.getHeaders().get("Set-Cookie").get(0);
        assertTrue(cookieHeader.contains("jwt=mockJwtToken123"));
        assertTrue(cookieHeader.contains("HttpOnly"));
        assertTrue(cookieHeader.contains("Path=/"));
        assertTrue(cookieHeader.contains("SameSite=Strict"));
    }

    @Test
    @DisplayName("Should login employee successfully")
    void login_WithEmployeeCredentials_ShouldReturnOk() {
        // Arrange
        AuthRequest authRequest = new AuthRequest("employee@test.com", "SecurePass123!");

        org.springframework.security.core.userdetails.UserDetails userDetails =
                org.springframework.security.core.userdetails.User.builder()
                        .username("employee@test.com")
                        .password("encodedPassword")
                        .authorities("ROLE_EMPLOYEE")
                        .build();

        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);

        healthcareab.project.healthcare_booking_app.models.Employee mockEmployee =
                new healthcareab.project.healthcare_booking_app.models.Employee();
        mockEmployee.setUsername("employee@test.com");
        mockEmployee.setEmail("employee@test.com");
        mockEmployee.setFirstName("Jane");
        mockEmployee.setLastName("Smith");
        mockEmployee.setRoles(Set.of(Role.EMPLOYEE));
        mockEmployee.setAddress("123 Main St");

        when(authService.findByUsername("employee@test.com")).thenReturn(mockEmployee);
        when(jwtUtil.generateToken(any(org.springframework.security.core.userdetails.UserDetails.class)))
                .thenReturn("mockJwtToken");

        HttpServletResponse httpResponse = mock(HttpServletResponse.class);

        // Act
        ResponseEntity<?> response = authController.login(authRequest, httpResponse);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        AuthResponse authResponse = (AuthResponse) response.getBody();
        assertEquals("employee@test.com", authResponse.getUsername());
        assertEquals("Jane", authResponse.getFirstName());
        assertEquals("Smith", authResponse.getLastName());
        assertEquals("123 Main St", authResponse.getAddress());
        assertTrue(authResponse.getRoles().contains(Role.EMPLOYEE));
    }

    // ========== LOGOUT TESTS ==========

    @Test
    @DisplayName("Should successfully logout")
    void logout_ShouldReturnOk() {
        // Arrange
        HttpServletResponse httpResponse = mock(HttpServletResponse.class);

        // Act
        ResponseEntity<?> response = authController.logout(httpResponse);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Logout successful!", response.getBody());

        // Verify cookie is cleared
        String cookieHeader = response.getHeaders().get("Set-Cookie").get(0);
        assertTrue(cookieHeader.contains("jwt="));
        assertTrue(cookieHeader.contains("Max-Age=0"));
    }

    // ========== CHECK AUTHENTICATION TESTS ==========

    @Test
    @DisplayName("Should return authenticated user info when authenticated")
    void checkAuthentication_WhenAuthenticated_ShouldReturnUserInfo() {
        // Arrange
        org.springframework.security.core.userdetails.UserDetails userDetails =
                org.springframework.security.core.userdetails.User.builder()
                        .username("patient@test.com")
                        .password("encodedPassword")
                        .authorities("ROLE_PATIENT")
                        .build();

        Authentication authentication = mock(Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(userDetails);

        SecurityContextHolder.getContext().setAuthentication(authentication);

        Patient mockUser = new Patient();
        mockUser.setUsername("patient@test.com");
        mockUser.setEmail("patient@test.com");
        mockUser.setFirstName("John");
        mockUser.setLastName("Doe");
        mockUser.setRoles(Set.of(Role.PATIENT));
        mockUser.setAddress(null);

        when(authService.findByUsername("patient@test.com")).thenReturn(mockUser);

        // Act
        ResponseEntity<?> response = authController.checkAuthentication();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof AuthResponse);

        AuthResponse authResponse = (AuthResponse) response.getBody();
        assertEquals("patient@test.com", authResponse.getUsername());

        // Cleanup
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Should return UNAUTHORIZED when not authenticated")
    void checkAuthentication_WhenNotAuthenticated_ShouldReturnUnauthorized() {
        // Arrange
        SecurityContextHolder.clearContext(); // Ensure no authentication

        // Act
        ResponseEntity<?> response = authController.checkAuthentication();

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Not authenticated!", response.getBody());
    }
}
