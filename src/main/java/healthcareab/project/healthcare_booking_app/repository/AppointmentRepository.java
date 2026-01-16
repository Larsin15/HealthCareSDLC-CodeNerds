package healthcareab.project.healthcare_booking_app.repository;

import healthcareab.project.healthcare_booking_app.models.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, UUID> {
}

