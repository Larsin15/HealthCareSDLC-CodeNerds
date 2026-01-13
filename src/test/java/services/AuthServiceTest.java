package services;

import healthcareab.project.healthcare_booking_app.models.Patient;
import healthcareab.project.healthcare_booking_app.models.Role;
import healthcareab.project.healthcare_booking_app.repository.UserRepository;
import healthcareab.project.healthcare_booking_app.services.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.Set;

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
}
