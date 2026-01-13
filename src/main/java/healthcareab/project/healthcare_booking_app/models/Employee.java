package healthcareab.project.healthcare_booking_app.models;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;

@Entity
@DiscriminatorValue("EMPLOYEE")
public class Employee extends User {

    @Column(name = "employee_number", unique = true, nullable = true, length = 5)
    @NotBlank(message = "Employee number is required")
    private String employeeNumber;

    @Column(name = "specialization", length = 100)
    private String specialization;

    @Column(name = "department", length = 100)
    private String department;

    @Column(name = "hirde_date")
    private LocalDate hireDate;

    @Column(name = "is_available_for_booking")
    private boolean availableForBooking = true;

    public Employee() {
        super();
    }

    //--------------------TEMPLATE METHODS --------------------------
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
 //------------------------------------------------------------------------------------------

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


    //------------------GETTERS & SETTERS---------------------------

    public String getEmployeeNumber() {
        return employeeNumber;
    }

    public void setEmployeeNumber(String employeeNumber) {
        this.employeeNumber = employeeNumber;
    }

    public String getSpecialization() {
        return specialization;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public LocalDate getHireDate() {
        return hireDate;
    }

    public void setHireDate(LocalDate hireDate) {
        this.hireDate = hireDate;
    }

    public boolean isAvailableForBooking() {
        return availableForBooking;
    }

    public void setAvailableForBooking(boolean availableForBooking) {
        this.availableForBooking = availableForBooking;
    }
}