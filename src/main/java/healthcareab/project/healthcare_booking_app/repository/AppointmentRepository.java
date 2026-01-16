package healthcareab.project.healthcare_booking_app.repository;

import healthcareab.project.healthcare_booking_app.models.Appointment;
import healthcareab.project.healthcare_booking_app.models.AppointmentStatus;
import healthcareab.project.healthcare_booking_app.models.Employee;
import healthcareab.project.healthcare_booking_app.models.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, UUID> {
    
    List<Appointment> findByPatient(Patient patient);
    
    List<Appointment> findByEmployee(Employee employee);
    
    List<Appointment> findByPatientAndStatus(Patient patient, AppointmentStatus status);
    
    List<Appointment> findByEmployeeAndStatus(Employee employee, AppointmentStatus status);
    
    @Query("SELECT a FROM Appointment a WHERE a.employee = :employee AND a.appointmentDate BETWEEN :startDate AND :endDate")
    List<Appointment> findByEmployeeAndDateRange(
        @Param("employee") Employee employee,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
    
    @Query("SELECT a FROM Appointment a WHERE a.patient = :patient AND a.appointmentDate BETWEEN :startDate AND :endDate")
    List<Appointment> findByPatientAndDateRange(
        @Param("patient") Patient patient,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
    
    @Query("SELECT a FROM Appointment a WHERE a.employee = :employee AND a.appointmentDate >= :startDate AND a.status IN :statuses")
    List<Appointment> findUpcomingByEmployeeAndStatuses(
        @Param("employee") Employee employee,
        @Param("startDate") LocalDateTime startDate,
        @Param("statuses") List<AppointmentStatus> statuses
    );
    
    @Query("SELECT a FROM Appointment a WHERE a.employee = :employee AND a.appointmentDate = :appointmentDate AND a.status != 'CANCELLED'")
    List<Appointment> findConflictingAppointments(
        @Param("employee") Employee employee,
        @Param("appointmentDate") LocalDateTime appointmentDate
    );
}
