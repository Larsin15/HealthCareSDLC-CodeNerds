package healthcareab.project.healthcare_booking_app.models;

public enum SlotStatus {

    AVAILABLE, // Available for patients to book

    BOOKED, // Has been booked by a patient

    CANCELLED, // Can be cancelled by employee (before booking) or by either party (after booking)

    COMPLETED // Set automatically after the appointment end time passes
}