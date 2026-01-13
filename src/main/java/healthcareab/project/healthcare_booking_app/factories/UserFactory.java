package healthcareab.project.healthcare_booking_app.factories;

import healthcareab.project.healthcare_booking_app.models.Employee;
import healthcareab.project.healthcare_booking_app.models.Patient;
import healthcareab.project.healthcare_booking_app.models.Role;
import healthcareab.project.healthcare_booking_app.models.User;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class UserFactory {

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
                Employee admin = new Employee();
                admin.setRoles(Set.of(Role.ADMIN));
                user = admin;
            }
            default -> throw new IllegalStateException("Unknown role: " + role);
        }

        user.setUsername(username);
        user.setPassword(password);
        user.setEmail(email);

        return user;
    }
}
