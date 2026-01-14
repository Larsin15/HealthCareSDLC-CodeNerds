package healthcareab.project.healthcare_booking_app.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
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

    @Column(name = "start_time", nullable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE")
    @NotNull(message = "Start time is required")
    private ZonedDateTime startTime;

    @Column(name = "end_time", nullable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE")
    @NotNull(message = "End time is required")
    private ZonedDateTime endTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SlotStatus status = SlotStatus.AVAILABLE;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public AvailabilitySlot() {
    }

    public AvailabilitySlot(Employee employee, ZonedDateTime startTime, ZonedDateTime endTime) {
        this.employee = employee;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = SlotStatus.AVAILABLE;
    }
}