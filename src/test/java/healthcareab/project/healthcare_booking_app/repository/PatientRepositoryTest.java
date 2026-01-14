package healthcareab.project.healthcare_booking_app.repository;

import healthcareab.project.healthcare_booking_app.models.Patient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@DataJpaTest
public class PatientRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private PatientRepository patientRepository;

    // Basic functionality tests
    @Test
    @DisplayName("Should find patient by id when patient exists")
    void findById_WhenPatientExists_ReturnsPatient() {
        // Arrange
        Patient patient = new Patient();
        patient.setFirstName("Alice");
        patient.setLastName("Smith");
        patient.setEmail("alice.smith@example.com");
        patient.setUsername("AliceSmith");
        patient.setPassword("SecurePass123!");
        patient.setPhoneNumber("0987654321");
        entityManager.persistAndFlush(patient);

        // Act
        Optional<Patient> found = patientRepository.findById(patient.getId());

        // Assert
        assertThat(found).isPresent();
        assertThat(found.get().getFirstName()).isEqualTo("Alice");
        assertThat(found.get().getLastName()).isEqualTo("Smith");
    }

    @Test
    @DisplayName("Should return empty when patient not found by id")
    void findById_WhenPatientNotFound_ReturnsEmpty() {
        // Act
        Optional<Patient> found = patientRepository.findById(UUID.randomUUID()); // Assuming this ID does not exist

        // Assert
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Should save patient successfully")
    void save_ValidPatient_ReturnsPatientWithId() {
        // Arrange
        Patient patient = new Patient();
        patient.setFirstName("Bob");
        patient.setLastName("Johnson");
        patient.setEmail("bob.johnson@example.com");
        patient.setUsername("BobJohnson");
        patient.setPassword("AnotherPass456!");
        patient.setPhoneNumber("1122334455");

        // Act
        Patient savedPatient = patientRepository.save(patient);

        // Assert
        assertThat(savedPatient.getId()).isNotNull();
        assertThat(savedPatient.getFirstName()).isEqualTo("Bob");
        assertThat(savedPatient.getLastName()).isEqualTo("Johnson");
    }

    @Test
    @DisplayName("Should delete patient by ID")
    void deleteById_WhenPatientExists_DeletesPatient() {
        // Arrange
        Patient patient = new Patient();
        patient.setFirstName("Charlie");
        patient.setLastName("Brown");
        patient.setEmail("charlie.brown@example.com");
        patient.setUsername("CharlieBrown");
        patient.setPassword("Password789!");
        patient.setPhoneNumber("5566778899");
        entityManager.persistAndFlush(patient);

        // Act
        patientRepository.deleteById(patient.getId());
        entityManager.flush();

        // Assert
        Optional<Patient> found = patientRepository.findById(patient.getId());
        assertThat(found).isEmpty();
    }

    // Edge cases

    @Test
    @DisplayName("Should throw exception when ID is null")
    void findById_WhenIdIsNull_ThrowsException() {
        // Act
        org.junit.jupiter.api.Assertions.assertThrows(
                org.springframework.dao.InvalidDataAccessApiUsageException.class,
                () -> patientRepository.findById(null)
        );
    }

    @Test
    @DisplayName("Should update existing patient details")
    void update_ExistingPatient_UpdatesDetails() {
        // Arrange
        Patient patient = new Patient();
        patient.setFirstName("David");
        patient.setLastName("Wilson");
        patient.setEmail("david.wilson@example");
        patient.setUsername("DavidWilson");
        patient.setPassword("InitPass123!");
        patient.setPhoneNumber("6677889900");
        Patient savedPatient = entityManager.persistAndFlush(patient);
        entityManager.clear();

        // Act
        patient.setEmail("david.wilson@example.com");
        Patient updatedPatient = patientRepository.save(patient);
        entityManager.flush();

        // Assert
        Optional<Patient> found = patientRepository.findById(updatedPatient.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("david.wilson@example.com");
    }

    @Test
    @DisplayName("Should handle special characters in patient details")
    void save_PatientWithSpecialCharacters_SavesSuccessfully() {
        // Arrange
        Patient patient = new Patient();
        patient.setFirstName("Elise");
        patient.setLastName("Connor");
        patient.setEmail("elise.connor@example.com");
        patient.setUsername("Elise@Connor!");
        patient.setPassword("Spec!alPass123");
        patient.setPhoneNumber("2233445566");

        // Act
        Patient savedPatient = patientRepository.save(patient);
        entityManager.flush();

        // Assert
        Optional<Patient> found = patientRepository.findById(savedPatient.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getUsername()).isEqualTo("Elise@Connor!");
        assertThat(found.get().getLastName()).isEqualTo("Connor");
    }
}
