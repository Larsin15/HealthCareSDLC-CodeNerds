package healthcareab.project.healthcare_booking_app.dto;


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
}

