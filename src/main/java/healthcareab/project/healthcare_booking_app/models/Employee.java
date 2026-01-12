import healthcareab.project.healthcare_booking_app.models.Role;
import healthcareab.project.healthcare_booking_app.models.User;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;

@Entity
@DiscriminatorValue("EMPLOYEE")
public class Employee extends User {

    @Column(name = "employee_number", unique = true, nullable = false, length = 20)
    @NotBlank(message = "Employee number is required")
    private String employeeNumber;

    @Column(name = "specialization", length = 100)
    private String specialization;

    @Column(name = "hirde_date")
    private LocalDate hireDate;

    @Column(name = "is_available_for_booking")
    private boolean availableForBooking = true;

    public Employee() {
        super();
    }

    @Override
    protected void onBeforeCreate() {
        super.onBeforeCreate(); //Call parent hook first

        if (getRoles() == null || getRoles().isEmpty()) {
            setRoles(java.util.Set.of(Role.EMPLOYEE));
        } else if (!getRoles().contains(Role.EMPLOYEE)) {
            // Here we add EMPLOYEE role
            java.util.Set<Role> roles = new java.util.HashSet<>(getRoles());
            roles.add(Role.EMPLOYEE);
            setRoles(roles);
        }
        // Set default hire date to today if not provided
        if (hireDate == null) {
            hireDate = LocalDate.now();
        }
    }

    @Override
    public void validateSpecificRules() {
        if (employeeNumber == null || employeeNumber.isEmpty()) {
            throw new IllegalArgumentException("Employee must have an employee number");
        }

        if (specialization == null || specialization.isEmpty()) {
            throw new IllegalArgumentException("Employee must have a specialization");
        }

        if (hireDate != null && hireDate.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Hire date cannot be in the future");
        }
    }

    /**
     * Checks if employee can accept bookings.
     *
     * Rules:
     * - Employee must be marked as available for booking
     * - Employee must have EMPLOYEE role
     *
     * Will be used when we check availability slots
     */
    public boolean canAcceptBookings() {
        return availableForBooking && getRoles() != null && getRoles().contains(Role.EMPLOYEE);
    }

}