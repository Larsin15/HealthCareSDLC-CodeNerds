package healthcareab.project.healthcare_booking_app.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

@Entity
@Table(name = "availability_slots", indexes = {
        @Index(name = "idx_employee_time", columnList = "employee_id, start_time, end_time"),
        @Index(name = "idx_status", columnList = "status")
})
public class AvailabilitySlot {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid")
    private UUID id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false) //Always load employee with slot, many slots can belong to one employee
    @JoinColumn(name = "employee_id", nullable = false)
    @NotNull(message = "Employee is required") //deleting employee should fail if slots exist (data integrity)
    private Employee employee;
}