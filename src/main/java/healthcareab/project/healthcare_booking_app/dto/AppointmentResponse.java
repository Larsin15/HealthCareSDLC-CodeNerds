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

    // getters and setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getAvailabilitySlotId() {
        return availabilitySlotId;
    }

    public void setAvailabilitySlotId(UUID availabilitySlotId) {
        this.availabilitySlotId = availabilitySlotId;
    }

    public ZonedDateTime getSlotStartTime() {
        return slotStartTime;
    }

    public void setSlotStartTime(ZonedDateTime slotStartTime) {
        this.slotStartTime = slotStartTime;
    }

    public ZonedDateTime getSlotEndTime() {
        return slotEndTime;
    }

    public void setSlotEndTime(ZonedDateTime slotEndTime) {
        this.slotEndTime = slotEndTime;
    }

    public UUID getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(UUID employeeId) {
        this.employeeId = employeeId;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }

    public String getEmployeeSpecialization() {
        return employeeSpecialization;
    }

    public void setEmployeeSpecialization(String employeeSpecialization) {
        this.employeeSpecialization = employeeSpecialization;
    }

    public UUID getPatientId() {
        return patientId;
    }

    public void setPatientId(UUID patientId) {
        this.patientId = patientId;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
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

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getCancelledAt() {
        return cancelledAt;
    }

    public void setCancelledAt(LocalDateTime cancelledAt) {
        this.cancelledAt = cancelledAt;
    }

    public boolean isCanCancel() {
        return canCancel;
    }

    public void setCanCancel(boolean canCancel) {
        this.canCancel = canCancel;
    }
}


