package healthcareab.project.healthcare_booking_app.factories;

import healthcareab.project.healthcare_booking_app.models.Employee;
import healthcareab.project.healthcare_booking_app.models.Patient;
import healthcareab.project.healthcare_booking_app.models.Role;
import healthcareab.project.healthcare_booking_app.models.User;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class UserFactory {

    // Encapsulates the creation logic for different user types
    public User createUser(Role role, String username, String password, String email) {

        User user;

        switch (role) {
            case PATIENT -> {
                Patient patient = new Patient();
                patient.setRoles(Set.of(Role.PATIENT));
                user = patient;
            }
            case EMPLOYEE -> {
                Employee employee = new Employee();
                employee.setRoles(Set.of(Role.EMPLOYEE));
                user = employee;
            }
            case ADMIN -> {
                // Admin is an employee with ADMIN role
                Employee admin = new Employee();
                admin.setRoles(Set.of(Role.ADMIN));
                user = admin;
            }
            default -> throw new IllegalArgumentException("Unknown role: " + role);
        }

        user.setUsername(username);
        user.setPassword(password);
        user.setEmail(email);

        return user;
    }

    public Patient createPatient(
            String username,
            String password,
            String email,
            String firstName,
            String lastName,
            java.time.LocalDate dateOfBirth) {

        Patient patient = (Patient) createUser(Role.PATIENT, username, password, email);
        patient.setFirstName(firstName);
        patient.setLastName(lastName);
        patient.setDateOfBirth(dateOfBirth);

        return patient;
    }

    public Employee createEmployee(
            String username,
            String password,
            String email,
            String firstName,
            String lastName,
            String employeeNumber,
            String specialization) {

        Employee employee = (Employee) createUser(Role.EMPLOYEE, username, password, email);
        employee.setFirstName(firstName);
        employee.setLastName(lastName);
        employee.setEmployeeNumber(employeeNumber);
        employee.setSpecialization(specialization);

        return employee;
    }
}
