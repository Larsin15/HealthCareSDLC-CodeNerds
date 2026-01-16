package healthcareab.project.healthcare_booking_app.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;


 // Contains the availability slot ID that the patient wants to book.
public class AppointmentRequest {

    @NotNull(message = "Availability slot ID is required")
    private UUID availabilitySlotId;

     // Optional notes from patient
     private String notes;

     public AppointmentRequest() {
     }

     public AppointmentRequest(UUID availabilitySlotId) {
         this.availabilitySlotId = availabilitySlotId;
     }

     public AppointmentRequest(UUID availabilitySlotId, String notes) {
         this.availabilitySlotId = availabilitySlotId;
         this.notes = notes;
     }

     public UUID getAvailabilitySlotId() {
         return availabilitySlotId;
     }

     public void setAvailabilitySlotId(UUID availabilitySlotId) {
         this.availabilitySlotId = availabilitySlotId;
     }

     public String getNotes() {
         return notes;
     }

     public void setNotes(String notes) {
         this.notes = notes;
     }
}

