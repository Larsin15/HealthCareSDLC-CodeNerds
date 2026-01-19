package healthcareab.project.healthcare_booking_app.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.Duration;
import java.util.UUID;

@Entity
@Table(name = "appointments", indexes = {
        @Index(name = "idx_appointment_patient", columnList = "patient_id"),
        @Index(name = "idx_appointment_employee", columnList = "employee_id"),
        @Index(name = "idx_appointment_status", columnList = "status")
})
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid")
    private UUID id;

    @OneToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "availability_slot_id", nullable = false, unique = true)
    @NotNull(message = "Availability slot is required")
    private AvailabilitySlot availabilitySlot;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "patient_id", nullable = false)
    @NotNull(message = "Patient is required")
    private Patient patient;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "employee_id", nullable = false)
    @NotNull(message = "Employee is required")
    private Employee employee;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AppointmentStatus status = AppointmentStatus.BOOKED;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    // constructor for JPA
    public Appointment() {
    }

    // Constructor for creating a new appointment
    public Appointment(AvailabilitySlot availabilitySlot, Patient patient, Employee employee) {
        this.availabilitySlot = availabilitySlot;
        this.patient = patient;
        this.employee = employee;
        this.status = AppointmentStatus.BOOKED;
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Checks if this appointment can be cancelled by a patient.
     * Patients can only cancel at least 24 hours before the appointment.
     *
     * @return true if the appointment can be cancelled by the patient
     */
    public boolean canBeCancelledByPatient() {
        if (status != AppointmentStatus.BOOKED) {
            return false;
        }
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("UTC"));
        ZonedDateTime slotStart = availabilitySlot.getStartTime();
        Duration timeUntilAppointment = Duration.between(now, slotStart);
        return timeUntilAppointment.toHours() >= 24;
    }

    /**
     * Checks if this appointment can be cancelled by an employee.
     * Employees can cancel anytime as long as the appointment is still booked.
     *
     * @return true if the appointment can be cancelled by the employee
     */
    public boolean canBeCancelledByEmployee() {
        return status == AppointmentStatus.BOOKED;
    }

    public boolean isInPast() {
        return availabilitySlot.getEndTime().isBefore(ZonedDateTime.now(ZoneId.of("UTC")));
    }

    public boolean isActive() {
        return status == AppointmentStatus.BOOKED && !isInPast();
    }

    public void cancel() {
        this.status = AppointmentStatus.CANCELLED;
        this.cancelledAt = LocalDateTime.now();
    }

    public void complete() {
        this.status = AppointmentStatus.COMPLETED;
    }

    //getters and setters

    public UUID getId() {
        return id;
    }

    public AvailabilitySlot getAvailabilitySlot() {
        return availabilitySlot;
    }

    public void setAvailabilitySlot(AvailabilitySlot availabilitySlot) {
        this.availabilitySlot = availabilitySlot;
    }

    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public AppointmentStatus getStatus() {
        return status;
    }

    public void setStatus(AppointmentStatus status) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public LocalDateTime getCancelledAt() {
        return cancelledAt;
    }

    public void setCancelledAt(LocalDateTime cancelledAt) {
        this.cancelledAt = cancelledAt;
    }

    // hashCode toString
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Appointment)) {
            return false;
        }
        Appointment that = (Appointment) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "Appointment{" +
                "id=" + id +
                ", patient=" + (patient != null ? patient.getUsername() : "null") +
                ", employee=" + (employee != null ? employee.getUsername() : "null") +
                ", slotTime=" + (availabilitySlot != null ? availabilitySlot.getStartTime() : "null") +
                ", status=" + status +
                '}';
    }

}




