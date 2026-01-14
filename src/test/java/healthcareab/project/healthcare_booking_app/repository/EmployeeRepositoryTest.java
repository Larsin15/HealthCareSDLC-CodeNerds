package healthcareab.project.healthcare_booking_app.repository;

import healthcareab.project.healthcare_booking_app.models.Employee;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@DataJpaTest
public class EmployeeRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private EmployeeRepository employeeRepository;

    // Basic CRUD tests

    @Test
    @DisplayName("Should find employee by id when employee exists")
    void findById_WhenEmployeeExists_ReturnsEmployee() {
        // Arrange
        Employee employee = new Employee();
        employee.setFirstName("Bob");
        employee.setLastName("Johnson");
        employee.setEmail("bob.johnson@example.com");
        employee.setUsername("BobJohnson");
        employee.setPassword("StrongPass456!");
        employee.setEmployeeNumber("EMP01");
        entityManager.persistAndFlush(employee);

        // Act
        Optional<Employee> found = employeeRepository.findById(employee.getId());

        // Assert
        assertThat(found).isPresent();
        assertThat(found.get().getFirstName()).isEqualTo("Bob");
        assertThat(found.get().getLastName()).isEqualTo("Johnson");
    }

    @Test
    @DisplayName("Should return empty when employee not found by id")
    void findById_WhenEmployeeNotFound_ReturnsEmpty() {
        // Act
        Optional<Employee> found = employeeRepository.findById(UUID.randomUUID()); // Assuming this ID doesnt exist

        // Assert
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Should save employee successfully")
    void save_ValidEmployee_ReturnsEmployeeWithId() {
        // Arrange
        Employee employee = new Employee();
        employee.setFirstName("Carol");
        employee.setLastName("Williams");
        employee.setEmail("carol.williams@example.com");
        employee.setUsername("CarolWilliams");
        employee.setPassword("Password789!");

        // Act
        Employee savedEmployee = employeeRepository.save(employee);

        // Assert
        assertThat(savedEmployee.getId()).isNotNull();
        assertThat(savedEmployee.getFirstName()).isEqualTo("Carol");
        assertThat(savedEmployee.getLastName()).isEqualTo("Williams");
    }

    @Test
    @DisplayName("Should delete employee by ID")
    void deleteById_ExistingEmployee_DeletesEmployee() {
        // Arrange
        Employee employee = new Employee();
        employee.setFirstName("David");
        employee.setLastName("Brown");
        employee.setEmail("david.brown@example.com");
        employee.setUsername("DavidBrown");
        employee.setPassword("SecurePass123!");
        employee.setEmployeeNumber("EMP01");
        entityManager.persistAndFlush(employee);

        // Act
        employeeRepository.deleteById(employee.getId());
        entityManager.flush();

        // Assert
        Optional<Employee> found = employeeRepository.findById(employee.getId());
        assertThat(found).isEmpty();
    }
}
