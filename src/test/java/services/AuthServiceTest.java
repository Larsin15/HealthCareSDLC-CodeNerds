package services;

import healthcareab.project.healthcare_booking_app.models.Employee;
import healthcareab.project.healthcare_booking_app.models.Patient;
import healthcareab.project.healthcare_booking_app.models.Role;
import healthcareab.project.healthcare_booking_app.models.User;
import healthcareab.project.healthcare_booking_app.repository.UserRepository;
import healthcareab.project.healthcare_booking_app.services.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService Unit Tests")
public class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    private Patient testPatient;

    @BeforeEach
    void setUp() {
        //Arrange: Setup test data
        testPatient = new Patient();
        testPatient.setUsername("JohnDoe12");
        testPatient.setPassword("password");
        testPatient.setEmail("john@doe.com");
        testPatient.setFirstName("John");
        testPatient.setLastName("Doe");
        testPatient.setPhoneNumber("+4670134567");
        testPatient.setDateOfBirth(LocalDate.of(1993,4,14));
        testPatient.setRoles(Set.of(Role.PATIENT));
    }

    @Test
    @DisplayName("Should successfully register a new patient")
    void registerUser_WithValidPatient_ShouldSaveUser() {
        //Arrange
        String originalPassword = testPatient.getPassword();
        String encodedPassword = "$2a$10$encodedPasswordHash";
        when(passwordEncoder.encode(testPatient.getPassword())).thenReturn(encodedPassword);
        when(userRepository.save(any(User.class))).thenReturn(testPatient);

        //Act
        authService.registerUser(testPatient);

        //Assert
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository, times(1)).save(userCaptor.capture());
        verify(passwordEncoder, times(1)).encode(originalPassword);

        User savedUser = userCaptor.getValue();
        assertEquals(encodedPassword, savedUser.getPassword(), "Password should be encoded");
        assertNotNull(savedUser.getRoles(), "User should have roles");
        assertTrue(savedUser.getRoles().contains(Role.PATIENT), "User should have PATIENT role");

    }

    @Test
    @DisplayName("Should encode password before saving")
    void registerUser_ShouldEncodePassword() {
        //Arange
        String plainPassword = "password";
        String encodedPassword = "$2a$10$encodedPasswordHash0";
        testPatient.setPassword(plainPassword);
        when(passwordEncoder.encode(plainPassword)).thenReturn(encodedPassword);
        when(userRepository.save(any(User.class))).thenReturn(testPatient);

        //Act
        authService.registerUser(testPatient);

        //Assert
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertEquals(encodedPassword, userCaptor.getValue().getPassword(), "Password should be encoded before saving");
    }


    @Test
    @DisplayName("Should set default PATIENT role if roles are null")
    void registerUser_WithNullRoles_ShouldSetDefaultRoles() {
        //Arrange
        testPatient.setRoles(null);
        when(passwordEncoder.encode(anyString())).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenReturn(testPatient);

        // Act
        authService.registerUser(testPatient);

        // Assert
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();
        assertNotNull(savedUser.getRoles(), "Roles should not be null");
        assertTrue(savedUser.getRoles().contains(Role.PATIENT),
                "Default role PATIENT should be set");
    }

    @Test
    @DisplayName("Should set default PATIENT role if roles are empty")
    void registerUser_WithEmptyRoles_ShouldSetDefaultRole() {
        // Arrange
        testPatient.setRoles(Set.of());
        when(passwordEncoder.encode(anyString())).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenReturn(testPatient);

        // Act
        authService.registerUser(testPatient);

        // Assert
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();
        assertTrue(savedUser.getRoles().contains(Role.PATIENT),
                "Default role PATIENT should be set");
    }

    @Test
    @DisplayName("Should validate user-specific rules before saving")
    void registerUser_WithInvalidPatient_ShouldThrowException() {
        // Arrange
        testPatient.setDateOfBirth(null); // Invalid: Patient must have date of birth
        when(passwordEncoder.encode(anyString())).thenReturn("encoded");

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            authService.registerUser(testPatient);
        }, "Should throw IllegalArgumentException when validation fails");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw exception when date of birth is in future")
    void registerUser_WithFutureDateOfBirth_ShouldThrowException() {
        // Arrange
        testPatient.setDateOfBirth(LocalDate.now().plusDays(1)); // Future date
        when(passwordEncoder.encode(anyString())).thenReturn("encoded");

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            authService.registerUser(testPatient);
        }, "Should throw IllegalArgumentException when date of birth is in future");

        verify(userRepository, never()).save(any(User.class));
    }

    // ========== EMPLOYEE TESTS ==========

    @Test
    @DisplayName("Should successfully register a new employee")
    void registerUser_WithValidEmployee_ShouldSaveUser() {
        // Arrange
        Employee testEmployee = new Employee();
        testEmployee.setUsername("employee@test.com");
        testEmployee.setPassword("SecurePass123!");
        testEmployee.setEmail("employee@test.com");
        testEmployee.setFirstName("Jane");
        testEmployee.setLastName("Smith");
        testEmployee.setEmployeeNumber("EMP001");
        testEmployee.setSpecialization("Cardiology");
        testEmployee.setRoles(Set.of(Role.EMPLOYEE));

        String originalPassword = testEmployee.getPassword();
        String encodedPassword = "$2a$10$encodedPasswordHash";
        when(passwordEncoder.encode(originalPassword)).thenReturn(encodedPassword);
        when(userRepository.save(any(User.class))).thenReturn(testEmployee);

        // Act
        authService.registerUser(testEmployee);

        // Assert
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository, times(1)).save(userCaptor.capture());
        verify(passwordEncoder, times(1)).encode(originalPassword);

        User savedUser = userCaptor.getValue();
        assertEquals(encodedPassword, savedUser.getPassword(), "Password should be encoded");
        assertTrue(savedUser instanceof Employee, "User should be an Employee");
        assertEquals("EMP001", ((Employee) savedUser).getEmployeeNumber(),
                "Employee number should be set");
        assertEquals("Cardiology", ((Employee) savedUser).getSpecialization(),
                "Specialization should be set");
        assertTrue(savedUser.getRoles().contains(Role.EMPLOYEE),
                "User should have EMPLOYEE role");
    }

    @Test
    @DisplayName("Should throw exception when employee number is missing")
    void registerUser_WithEmployeeMissingEmployeeNumber_ShouldThrowException() {
        // Arrange
        Employee testEmployee = new Employee();
        testEmployee.setUsername("employee@test.com");
        testEmployee.setPassword("SecurePass123!");
        testEmployee.setEmail("employee@test.com");
        testEmployee.setFirstName("Jane");
        testEmployee.setLastName("Smith");
        testEmployee.setEmployeeNumber(null); // Missing!
        testEmployee.setSpecialization("Cardiology");
        testEmployee.setRoles(Set.of(Role.EMPLOYEE));

        when(passwordEncoder.encode(anyString())).thenReturn("encoded");

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            authService.registerUser(testEmployee);
        }, "Should throw IllegalArgumentException when employee number is missing");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw exception when employee number is empty")
    void registerUser_WithEmployeeEmptyEmployeeNumber_ShouldThrowException() {
        // Arrange
        Employee testEmployee = new Employee();
        testEmployee.setUsername("employee@test.com");
        testEmployee.setPassword("SecurePass123!");
        testEmployee.setEmail("employee@test.com");
        testEmployee.setFirstName("Jane");
        testEmployee.setLastName("Smith");
        testEmployee.setEmployeeNumber(""); // Empty!
        testEmployee.setSpecialization("Cardiology");
        testEmployee.setRoles(Set.of(Role.EMPLOYEE));

        when(passwordEncoder.encode(anyString())).thenReturn("encoded");

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            authService.registerUser(testEmployee);
        }, "Should throw IllegalArgumentException when employee number is empty");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw exception when specialization is missing")
    void registerUser_WithEmployeeMissingSpecialization_ShouldThrowException() {
        // Arrange
        Employee testEmployee = new Employee();
        testEmployee.setUsername("employee@test.com");
        testEmployee.setPassword("SecurePass123!");
        testEmployee.setEmail("employee@test.com");
        testEmployee.setFirstName("Jane");
        testEmployee.setLastName("Smith");
        testEmployee.setEmployeeNumber("EMP001");
        testEmployee.setSpecialization(null); // Missing!
        testEmployee.setRoles(Set.of(Role.EMPLOYEE));

        when(passwordEncoder.encode(anyString())).thenReturn("encoded");

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            authService.registerUser(testEmployee);
        }, "Should throw IllegalArgumentException when specialization is missing");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw exception when specialization is empty")
    void registerUser_WithEmployeeEmptySpecialization_ShouldThrowException() {
        // Arrange
        Employee testEmployee = new Employee();
        testEmployee.setUsername("employee@test.com");
        testEmployee.setPassword("SecurePass123!");
        testEmployee.setEmail("employee@test.com");
        testEmployee.setFirstName("Jane");
        testEmployee.setLastName("Smith");
        testEmployee.setEmployeeNumber("EMP001");
        testEmployee.setSpecialization(""); // Empty!
        testEmployee.setRoles(Set.of(Role.EMPLOYEE));

        when(passwordEncoder.encode(anyString())).thenReturn("encoded");

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            authService.registerUser(testEmployee);
        }, "Should throw IllegalArgumentException when specialization is empty");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw exception when hire date is in future")
    void registerUser_WithFutureHireDate_ShouldThrowException() {
        // Arrange
        Employee testEmployee = new Employee();
        testEmployee.setUsername("employee@test.com");
        testEmployee.setPassword("SecurePass123!");
        testEmployee.setEmail("employee@test.com");
        testEmployee.setFirstName("Jane");
        testEmployee.setLastName("Smith");
        testEmployee.setEmployeeNumber("EMP001");
        testEmployee.setSpecialization("Cardiology");
        testEmployee.setHireDate(LocalDate.now().plusDays(1)); // Future date!
        testEmployee.setRoles(Set.of(Role.EMPLOYEE));

        when(passwordEncoder.encode(anyString())).thenReturn("encoded");

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            authService.registerUser(testEmployee);
        }, "Should throw IllegalArgumentException when hire date is in future");

        verify(userRepository, never()).save(any(User.class));
    }

    // ========== ADMIN TESTS ==========

    @Test
    @DisplayName("Should successfully register a new admin")
    void registerUser_WithValidAdmin_ShouldSaveUser() {
        // Arrange
        Employee testAdmin = new Employee();
        testAdmin.setUsername("admin@test.com");
        testAdmin.setPassword("SecurePass123!");
        testAdmin.setEmail("admin@test.com");
        testAdmin.setFirstName("Admin");
        testAdmin.setLastName("User");
        testAdmin.setEmployeeNumber("ADM001");
        testAdmin.setSpecialization("Administration");
        testAdmin.setDepartment("Management");
        testAdmin.setRoles(Set.of(Role.ADMIN));

        String originalPassword = testAdmin.getPassword();
        String encodedPassword = "$2a$10$encodedPasswordHash";
        when(passwordEncoder.encode(originalPassword)).thenReturn(encodedPassword);
        when(userRepository.save(any(User.class))).thenReturn(testAdmin);

        // Act
        authService.registerUser(testAdmin);

        // Assert
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository, times(1)).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();
        assertEquals(encodedPassword, savedUser.getPassword(), "Password should be encoded");
        assertTrue(savedUser instanceof Employee, "Admin should be an Employee");
        assertEquals("ADM001", ((Employee) savedUser).getEmployeeNumber());
        assertTrue(savedUser.getRoles().contains(Role.ADMIN),
                "User should have ADMIN role");
    }

}
