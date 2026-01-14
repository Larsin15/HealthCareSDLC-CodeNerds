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

    @Override
    public String toString() {
        return "AvailabilitySlotRequest{" + //representation for logging and debugging
                "startTime=" + startTime +
                ", endTime=" + endTime +
                '}';
    }
}