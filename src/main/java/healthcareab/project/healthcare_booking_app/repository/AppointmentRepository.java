package healthcareab.project.healthcare_booking_app.repository;

import healthcareab.project.healthcare_booking_app.models.Appointment;
import healthcareab.project.healthcare_booking_app.models.AppointmentStatus;
import healthcareab.project.healthcare_booking_app.models.AvailabilitySlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, UUID> {

    List<Appointment> findByPatientId(UUID patientId);

    @Query("SELECT a FROM Appointment a WHERE a.patient.id = :patientId " +
            "ORDER BY a.availabilitySlot.startTime DESC")
    List<Appointment> findByPatientIdOrderBySlotStartTimeDesc(@Param("patientId") UUID patientId);

    List<Appointment> findByEmployeeId(UUID employeeId);

    @Query("SELECT a FROM Appointment a WHERE a.employee.id = :employeeId " +
            "ORDER BY a.availabilitySlot.startTime DESC")
    List<Appointment> findByEmployeeIdOrderBySlotStartTimeDesc(@Param("employeeId") UUID employeeId);

    List<Appointment> findByPatientIdAndStatus(UUID patientId, AppointmentStatus status);

    List<Appointment> findByEmployeeIdAndStatus(UUID employeeId, AppointmentStatus status);

    long countByPatientIdAndStatusIn(UUID patientId, List<AppointmentStatus> statuses);

    @Query("SELECT COUNT(a) > 0 FROM Appointment a " +
            "WHERE a.patient.id = :patientId AND a.status = 'BOOKED'")
    boolean hasActiveBooking(@Param("patientId") UUID patientId);

    Optional<Appointment> findByAvailabilitySlot(AvailabilitySlot availabilitySlot);

    @Query("SELECT a FROM Appointment a WHERE a.availabilitySlot.id = :slotId")
    Optional<Appointment> findByAvailabilitySlotId(@Param("slotId") UUID slotId);

    @Query("SELECT COUNT(a) > 0 FROM Appointment a WHERE a.availabilitySlot.id = :slotId")
    boolean existsByAvailabilitySlotId(@Param("slotId") UUID slotId);
}


