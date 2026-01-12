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
    }

}