package healthcareab.project.healthcare_booking_app.models;

import healthcareab.project.healthcare_booking_app.converters.SwedishTimeConverter;
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

    @Column(name = "start_time", nullable = false, columnDefinition = "TIMESTAMP WITHOUT TIME ZONE")
    @Convert(converter = SwedishTimeConverter.class)
    @NotNull(message = "Start time is required")
    private ZonedDateTime startTime;

    @Column(name = "end_time", nullable = false, columnDefinition = "TIMESTAMP WITHOUT TIME ZONE")
    @Convert(converter = SwedishTimeConverter.class)
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

    @PrePersist
    protected void onCreate() { // Sets creation and update timestamps
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() { // Updates the modification timestamp
        updatedAt = LocalDateTime.now();
    }

    public boolean isAvailableForBooking() {
        return status == SlotStatus.AVAILABLE
                && employee != null
                && employee.canAcceptBookings();
    }

    public boolean canBeCancelled() {
        return status == SlotStatus.AVAILABLE || status == SlotStatus.BOOKED; //true if status is AVAILABLE or BOOKED (not already cancelled/completed)
    }

    public boolean isInPast() { //Checks if this slot is in the past
        return endTime.isBefore(ZonedDateTime.now());
    }

    public long getDurationMinutes() { // Duration in minutes (should always be 30)
        return java.time.Duration.between(startTime, endTime).toMinutes();
    }

    public UUID getId() {
        return id;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public ZonedDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(ZonedDateTime startTime) {
        this.startTime = startTime;
    }

    public ZonedDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(ZonedDateTime endTime) {
        this.endTime = endTime;
    }

    public SlotStatus getStatus() {
        return status;
    }

    public void setStatus(SlotStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    /**
     * Two slots are equal if they have the same ID
     * Important for JPA entity management and collections
     */

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AvailabilitySlot)) {
            return false;
        }
        AvailabilitySlot that = (AvailabilitySlot) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "AvailabilitySlot{" +
                "id=" + id +
                ", employee=" + (employee != null ? employee.getUsername() : "null") +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", status=" + status +
                '}';
    }
}