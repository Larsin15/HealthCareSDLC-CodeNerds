package healthcareab.project.healthcare_booking_app.repository;

import healthcareab.project.healthcare_booking_app.models.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PatientRepository extends JpaRepository<Patient, UUID> {

    Optional<Patient> findByDateOfBirth(LocalDate dateOfBirth);

    boolean existsByDateOfBirth(LocalDate dateOfBirth);

    List<Patient> findByFirstNameIgnoreCase(String firstName);

    List<Patient> findByLastNameIgnoreCase(String lastName);

    List<Patient> findByFirstNameIgnoreCaseAndLastNameIgnoreCase(String firstName, String lastName);

}