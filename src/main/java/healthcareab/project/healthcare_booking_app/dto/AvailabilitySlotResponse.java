package healthcareab.project.healthcare_booking_app.dto;

import healthcareab.project.healthcare_booking_app.models.AvailabilitySlot;
import healthcareab.project.healthcare_booking_app.models.SlotStatus;

public class AvailabilitySlotResponse {

    private UUID id;

    private UUID employeeId;

    private String employeeName; // "FirstName LastName"

    private String employeeSpecialization; // Employee specialization ("Läkare", "SSK", "USK")

    private ZonedDateTime startTime;

    private ZonedDateTime endTime;

    private SlotStatus status;

    public AvailabilitySlotResponse() {
    }

    public AvailabilitySlotResponse(AvailabilitySlot slot) {
        this.id = slot.getId();
        this.employeeId = slot.getEmployee().getId();
        this.employeeName = slot.getEmployee().getFirstName() + " " + slot.getEmployee().getLastName();
        this.employeeSpecialization = slot.getEmployee().getSpecialization();
        this.startTime = slot.getStartTime();
        this.endTime = slot.getEndTime();
        this.status = slot.getStatus();
    }

    public AvailabilitySlotResponse( // Full constructor for manual testing creation
            UUID id,
            UUID employeeId,
            String employeeName,
            String employeeSpecialization,
            ZonedDateTime startTime,
            ZonedDateTime endTime,
            SlotStatus status
    ) {
        this.id = id;
        this.employeeId = employeeId;
        this.employeeName = employeeName;
        this.employeeSpecialization = employeeSpecialization;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status;
    }
}

