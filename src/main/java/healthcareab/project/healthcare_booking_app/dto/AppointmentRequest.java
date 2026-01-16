package healthcareab.project.healthcare_booking_app.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;


 // Contains the availability slot ID that the patient wants to book.
public class AppointmentRequest {

    @NotNull(message = "Availability slot ID is required")
    private UUID availabilitySlotId;
}

