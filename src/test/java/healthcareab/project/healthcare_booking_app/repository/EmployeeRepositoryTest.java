package healthcareab.project.healthcare_booking_app.repository;

import healthcareab.project.healthcare_booking_app.models.Employee;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.dao.InvalidDataAccessApiUsageException;

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
        employee.setEmployeeNumber("EMP02");

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

    // Edge cases

    @Test
    @DisplayName("Should throw exception when ID is null")
    void findById_WhenIdIsNull_ThrowsException() {
        // Act & Assert
        Assertions.assertThrows(
                InvalidDataAccessApiUsageException.class,
                () -> employeeRepository.findById(null)
        );
    }

    @Test
    @DisplayName("Should update existing employee details")
    void update_ExistingEmployee_UpdatesDetails() {
        // Arrange
        Employee employee = new Employee();
        employee.setFirstName("Eve");
        employee.setLastName("Davis");
        employee.setEmail("eve.davis@example");
        employee.setUsername("EveDavis");
        employee.setPassword("InitPass123!");
        employee.setEmployeeNumber("EMP02");
        Employee savedEmployee = entityManager.persistAndFlush(employee);
        entityManager.clear();

        // Act
        employee.setEmail("eve.davis@example.com");
        Employee updatedEmployee = employeeRepository.save(employee);
        entityManager.flush();

        // Assert
        Optional<Employee> found = employeeRepository.findById(savedEmployee.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("eve.davis@example.com");
    }

    @Test
    @DisplayName("Should handle special characters in employee number")
    void save_EmployeeWithSpecialCharactersInEmployeeNumber_SavesSuccessfully() {
        // Arrange
        Employee employee = new Employee();
        employee.setFirstName("Frank");
        employee.setLastName("Miller");
        employee.setEmail("frank.miller@example.com");
        employee.setUsername("FrankMiller");
        employee.setPassword("Password123!");
        employee.setEmployeeNumber("EMP0$");

        // Act
        Employee savedEmployee = employeeRepository.save(employee);
        entityManager.flush();

        // Assert
        Optional<Employee> found = employeeRepository.findById(savedEmployee.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getEmployeeNumber()).isEqualTo("EMP0$");
        assertThat(found.get().getFirstName()).isEqualTo("Frank");
    }

    // Custom Query tests

    @Test
    @DisplayName("Should find employee by employee number when employee exists")
    void findByEmployeeNumber_WhenEmployeeExists_ReturnsEmployee() {
        // Arrange
        Employee employee = new Employee();
        employee.setFirstName("Grace");
        employee.setLastName("Lee");
        employee.setEmail("grace.lee@example.com");
        employee.setUsername("GraceLee");
        employee.setPassword("Pass456!");
        employee.setEmployeeNumber("EMP03");
        entityManager.persistAndFlush(employee);

        // Act
        Optional<Employee> found = employeeRepository.findByEmployeeNumber("EMP03");

        // Assert
        assertThat(found).isPresent();
        assertThat(found.get().getEmployeeNumber()).isEqualTo("EMP03");
    }

    @Test
    @DisplayName("Should return true when employee exists by employee number")
    void existsByEmployeeNumber_WhenEmployeeExists_ReturnsTrue() {
        // Arrange
        Employee employee = new Employee();
        employee.setFirstName("Hank");
        employee.setLastName("Wilson");
        employee.setEmail("hank.wilson@example.com");
        employee.setUsername("HankWilson");
        employee.setPassword("Pass789!");
        employee.setEmployeeNumber("EMP04");
        entityManager.persistAndFlush(employee);

        // Act
        boolean exists = employeeRepository.existsByEmployeeNumber("EMP04");

        // Assert
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("Should return false when employee does not exist by employee number")
    void existsByEmployeeNumber_WhenEmployeeNotExists_ReturnsFalse() {
        // Act
        boolean exists = employeeRepository.existsByEmployeeNumber("NONEXISTENT");

        // Assert
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("Should find employees by specialization")
    void findBySpecialization_WhenEmployeesExist_ReturnsEmployees() {
        // Arrange
        Employee emp1 = new Employee();
        emp1.setFirstName("Ivy");
        emp1.setLastName("Taylor");
        emp1.setEmail("ivy.taylor@example.com");
        emp1.setUsername("IvyTaylor");
        emp1.setPassword("SpecPass123!");
        emp1.setEmployeeNumber("EMP05");
        emp1.setSpecialization("Cardiology");
        entityManager.persistAndFlush(emp1);

        Employee emp2 = new Employee();
        emp2.setFirstName("Jack");
        emp2.setLastName("Anderson");
        emp2.setEmail("jack.anderson@example.com");
        emp2.setUsername("JackAnderson");
        emp2.setPassword("SpecPass456!");
        emp2.setEmployeeNumber("EMP06");
        emp2.setSpecialization("Cardiology");
        entityManager.persistAndFlush(emp2);

        // Act
        var foundEmployees = employeeRepository.findBySpecialization("Cardiology");

        // Assert
        assertThat(foundEmployees).isNotNull();
        assertThat(foundEmployees.size()).isEqualTo(2);
    }

    @Test
    @DisplayName("Should find employees by department")
    void findByDepartment_WhenEmployeesExist_ReturnsEmployees() {
        // Arrange
        Employee emp1 = new Employee();
        emp1.setFirstName("Karen");
        emp1.setLastName("Thomas");
        emp1.setEmail("karen.thomas@example.com");
        emp1.setUsername("KarenThomas");
        emp1.setPassword("DeptPass123!");
        emp1.setEmployeeNumber("EMP07");
        emp1.setDepartment("Radiology");
        entityManager.persistAndFlush(emp1);

        Employee emp2 = new Employee();
        emp2.setFirstName("Leo");
        emp2.setLastName("Moore");
        emp2.setEmail("leo.moore@example.com");
        emp2.setUsername("LeoMoore");
        emp2.setPassword("DeptPass456!");
        emp2.setEmployeeNumber("EMP08");
        emp2.setDepartment("Radiology");
        entityManager.persistAndFlush(emp2);

        // Act
        var foundEmployees = employeeRepository.findByDepartment("Radiology");

        // Assert
        assertThat(foundEmployees).isNotNull();
        assertThat(foundEmployees.size()).isEqualTo(2);
    }

    @Test
    @DisplayName("Should find all available employees")
    void findByAvailableTrue_WhenEmployeesExist_ReturnsAvailableEmployees() {
        // Arrange
        Employee emp1 = new Employee();
        emp1.setFirstName("Mia");
        emp1.setLastName("Jackson");
        emp1.setEmail("mia.jackson@example.com");
        emp1.setUsername("MiaJackson");
        emp1.setPassword("AvailPass123!");
        emp1.setEmployeeNumber("EMP09");
        emp1.setAvailableForBooking(true);
        entityManager.persistAndFlush(emp1);

        Employee emp2 = new Employee();
        emp2.setFirstName("Noah");
        emp2.setLastName("White");
        emp2.setEmail("noah.white@example.com");
        emp2.setUsername("NoahWhite");
        emp2.setPassword("AvailPass456!");
        emp2.setEmployeeNumber("EMP10");
        emp2.setAvailableForBooking(false);
        entityManager.persistAndFlush(emp2);

        // Act
        var foundEmployees = employeeRepository.findAllAvailableEmployees();

        // Assert
        assertThat(foundEmployees).isNotNull();
        assertThat(foundEmployees.size()).isEqualTo(1);
        assertThat(foundEmployees.get(0).getFirstName()).isEqualTo("Mia");
    }

    @Test
    @DisplayName("Should find available employees by specialization")
    void findByAvailableTrueAndSpecialization_WhenEmployeesExist_ReturnsAvailableEmployeesBySpecialization() {
        // Arrange
        Employee emp1 = new Employee();
        emp1.setFirstName("Olivia");
        emp1.setLastName("Harris");
        emp1.setEmail("olivia.harris@example.com");
        emp1.setUsername("OliviaHarris");
        emp1.setPassword("AvailSpecPass123!");
        emp1.setEmployeeNumber("EMP11");
        emp1.setAvailableForBooking(true);
        emp1.setSpecialization("Dermatology");
        entityManager.persistAndFlush(emp1);

        Employee emp2 = new Employee();
        emp2.setFirstName("Paul");
        emp2.setLastName("Martin");
        emp2.setEmail("paul.martin@example.com");
        emp2.setUsername("PaulMartin");
        emp2.setPassword("AvailSpecPass456!");
        emp2.setEmployeeNumber("EMP12");
        emp2.setAvailableForBooking(true);
        emp2.setSpecialization("Dermatology");
        entityManager.persistAndFlush(emp2);

        Employee emp3 = new Employee();
        emp3.setFirstName("Quinn");
        emp3.setLastName("Thompson");
        emp3.setEmail("quinn.thompson@example.com");
        emp3.setUsername("QuinnThompson");
        emp3.setPassword("AvailSpecPass789!");
        emp3.setEmployeeNumber("EMP13");
        emp3.setAvailableForBooking(false);
        emp3.setSpecialization("Dermatology");
        entityManager.persistAndFlush(emp3);

        // Act
        var foundEmployees = employeeRepository.findAvailableEmployeesBySpecialization("Dermatology");

        // Assert
        assertThat(foundEmployees).isNotNull();
        assertThat(foundEmployees.size()).isEqualTo(2);
        assertThat(foundEmployees.get(0).getFirstName()).isEqualTo("Olivia");
        assertThat(foundEmployees.get(1).getFirstName()).isEqualTo("Paul");
    }
}
