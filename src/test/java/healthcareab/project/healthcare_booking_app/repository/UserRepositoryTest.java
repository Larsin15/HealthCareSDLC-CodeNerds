package healthcareab.project.healthcare_booking_app.repository;

import healthcareab.project.healthcare_booking_app.models.Patient;
import healthcareab.project.healthcare_booking_app.models.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@DataJpaTest
public class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager; // For arranging test data

    @Autowired
    private UserRepository userRepository; // Repository to be tested

    @Test
    @DisplayName("Should find user by email when user exists")
    void findByEmail_WhenUserExists_ReturnsUser() {
        // Arrange
        Patient patient = new Patient();
        patient.setFirstName("John");
        patient.setLastName("Doe");
        patient.setEmail("John.Doe@test.com");
        patient.setUsername("JohnDoe");
        patient.setPassword("Password123#");
        patient.setPhoneNumber("1234567890");
        entityManager.persistAndFlush(patient);

        // Act
        Optional<User> found = userRepository.findByEmail("John.Doe@test.com");

        // Assert
        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("John.Doe@test.com");
    }

    @Test
    @DisplayName("Should return empty when user not found by email")
    void findByEmail_WhenUserNotFound_ReturnsEmpty() {
        // Arrange
        // No user is added to the test database

        // Act
        Optional<User> found = userRepository.findByEmail("nonexistent@test.com");

        // Assert
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Should find user by username when user exists")
    void findByUsername_WhenUserExists_ReturnsUser() {
        // Arrange
        Patient patient = new Patient();
        patient.setFirstName("John");
        patient.setLastName("Doe");
        patient.setEmail("John.Doe@Test.com");
        patient.setUsername("JohnDoe");
        patient.setPassword("Password123#");
        patient.setPhoneNumber("1234567890");
        entityManager.persistAndFlush(patient);

        // Act
        Optional<User> found = userRepository.findByUsername("JohnDoe");

        // Assert
        assertThat(found).isPresent();
        assertThat(found.get().getUsername()).isEqualTo("JohnDoe");
    }

    @Test
    @DisplayName("Should return empty when user not found by username")
    void findByUsername_WhenUserNotFound_ReturnsEmpty() {
        // Arrange
        // No user is added to the test database

        // Act
        Optional<User> found = userRepository.findByUsername("NonExistentUser");

        // Assert
        assertThat(found).isEmpty();
    }
}
