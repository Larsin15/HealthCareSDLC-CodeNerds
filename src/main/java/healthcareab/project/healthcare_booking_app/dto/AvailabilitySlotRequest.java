package healthcareab.project.healthcare_booking_app.dto;

import jakarta.validation.constraints.NotNull;

import java.time.ZonedDateTime;

public class AvailabilitySlotRequest {

    @NotNull(message = "Start time is required")
    private ZonedDateTime startTime;

    @NotNull(message = "End time is required")
    private ZonedDateTime endTime;

    public AvailabilitySlotRequest() {
    }

    public AvailabilitySlotRequest(ZonedDateTime startTime, ZonedDateTime endTime) {
        this.startTime = startTime;
        this.endTime = endTime;
    }
}