package healthcareab.project.healthcare_booking_app.repository;

import healthcareab.project.healthcare_booking_app.models.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, UUID> {

    Optional<Employee> findByEmployeeNumber(String employeeNumber);

    boolean existsByEmployeeNumber(String employeeNumber);

    List<Employee> findBySpecialization(String specialization);

    List<Employee> findByDepartment(String department);

    @Query("SELECT e FROM Employee e WHERE e.availableForBooking = true")
    List<Employee> findAllAvailableEmployees();

    @Query("SELECT e FROM Employee e WHERE e.availableForBooking = true AND e.specialization = :specialization")
    List<Employee> findAvailableEmployeesBySpecialization(String specialization);

}