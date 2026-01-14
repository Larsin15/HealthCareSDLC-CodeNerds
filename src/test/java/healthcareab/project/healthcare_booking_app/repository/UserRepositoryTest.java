package healthcareab.project.healthcare_booking_app.repository;

import healthcareab.project.healthcare_booking_app.models.Patient;
import healthcareab.project.healthcare_booking_app.models.User;
import healthcareab.project.healthcare_booking_app.repository.UserRepository;
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
        patient.setEmail("test@test.com");
        patient.setUsername("Test User");
        patient.setPassword("Password123#");
        entityManager.persistAndFlush(patient);

        // Act
        Optional<User> found = userRepository.findByEmail("test@test.com");

        // Assert
        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("test@test.com");
        }
}
