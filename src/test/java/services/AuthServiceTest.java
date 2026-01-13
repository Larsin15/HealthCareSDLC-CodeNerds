package services;

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
        String encodedPassword = "$2a$10$encodedPasswordHash";
        when(passwordEncoder.encode(testPatient.getPassword())).thenReturn(encodedPassword);
        when(userRepository.save(any(User.class))).thenReturn(testPatient);

        //Act
        authService.registerUser(testPatient);

        //Assert
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository, times(1)).save(userCaptor.capture());
        verify(passwordEncoder, times(1)).encode(testPatient.getPassword());

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

}
