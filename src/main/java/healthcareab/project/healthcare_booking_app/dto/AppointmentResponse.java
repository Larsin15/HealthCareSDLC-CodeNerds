package healthcareab.project.healthcare_booking_app.dto;


import healthcareab.project.healthcare_booking_app.models.Appointment;
import healthcareab.project.healthcare_booking_app.models.AppointmentStatus;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.UUID;

public class AppointmentResponse {

    private UUID id;
    private UUID availabilitySlotId;
    private ZonedDateTime slotStartTime;
    private ZonedDateTime slotEndTime;

    // Employee information
    private UUID employeeId;
    private String employeeName;
    private String employeeSpecialization;

    // Patient information
    private UUID patientId;
    private String patientName;

    // Appointment information
    private AppointmentStatus status;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime cancelledAt;

    private boolean canCancel;

    public AppointmentResponse() {
    }

    //Constructor from Appointment entity for patient view
    public static AppointmentResponse forPatient(Appointment appointment) {
        AppointmentResponse response = new AppointmentResponse();
        response.id = appointment.getId();
        response.availabilitySlotId = appointment.getAvailabilitySlot().getId();
        response.slotStartTime = appointment.getAvailabilitySlot().getStartTime();
        response.slotEndTime = appointment.getAvailabilitySlot().getEndTime();

        response.employeeId = appointment.getEmployee().getId();
        response.employeeName = appointment.getEmployee().getFirstName() + " "
                + appointment.getEmployee().getLastName();
        response.employeeSpecialization = appointment.getEmployee().getSpecialization();

        response.patientId = appointment.getPatient().getId();
        response.patientName = appointment.getPatient().getFirstName() + " "
                + appointment.getPatient().getLastName();

        response.status = appointment.getStatus();
        response.notes = appointment.getNotes();
        response.createdAt = appointment.getCreatedAt();
        response.cancelledAt = appointment.getCancelledAt();

        response.canCancel = appointment.canBeCancelledByPatient();

        return response;
    }

    // Constructor from appointment for employee view (includes patient name for the employee to see who booked)
    public static AppointmentResponse forEmployee(Appointment appointment) {
        AppointmentResponse response = new AppointmentResponse();
        response.id = appointment.getId();
        response.availabilitySlotId = appointment.getAvailabilitySlot().getId();
        response.slotStartTime = appointment.getAvailabilitySlot().getStartTime();
        response.slotEndTime = appointment.getAvailabilitySlot().getEndTime();

        response.employeeId = appointment.getEmployee().getId();
        response.employeeName = appointment.getEmployee().getFirstName() + " "
                + appointment.getEmployee().getLastName();
        response.employeeSpecialization = appointment.getEmployee().getSpecialization();

        response.patientId = appointment.getPatient().getId();
        response.patientName = appointment.getPatient().getFirstName() + " "
                + appointment.getPatient().getLastName();

        response.status = appointment.getStatus();
        response.notes = appointment.getNotes();
        response.createdAt = appointment.getCreatedAt();
        response.cancelledAt = appointment.getCancelledAt();

        // Employees can cancel anytime
        response.canCancel = appointment.canBeCancelledByEmployee();

        return response;
    }

    // Constructor for manual creation (testing)
    public AppointmentResponse(
            UUID id,
            UUID availabilitySlotId,
            ZonedDateTime slotStartTime,
            ZonedDateTime slotEndTime,
            UUID employeeId,
            String employeeName,
            String employeeSpecialization,
            UUID patientId,
            String patientName,
            AppointmentStatus status,
            String notes,
            LocalDateTime createdAt,
            LocalDateTime cancelledAt,
            boolean canCancel
    ) {
        this.id = id;
        this.availabilitySlotId = availabilitySlotId;
        this.slotStartTime = slotStartTime;
        this.slotEndTime = slotEndTime;
        this.employeeId = employeeId;
        this.employeeName = employeeName;
        this.employeeSpecialization = employeeSpecialization;
        this.patientId = patientId;
        this.patientName = patientName;
        this.status = status;
        this.notes = notes;
        this.createdAt = createdAt;
        this.cancelledAt = cancelledAt;
        this.canCancel = canCancel;
    }
}

