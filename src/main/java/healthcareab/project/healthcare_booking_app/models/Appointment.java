package healthcareab.project.healthcare_booking_app.models;

import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

@Entity
@Table(name = "appointments", indexes = {
        @Index(name = "idx_appointment_patient", columnList = "patient_id"),
        @Index(name = "idx_appointment_employee", columnList = "employee_id"),
        @Index(name = "idx_appointment_status", columnList = "status")
})
public class Appointment {}



