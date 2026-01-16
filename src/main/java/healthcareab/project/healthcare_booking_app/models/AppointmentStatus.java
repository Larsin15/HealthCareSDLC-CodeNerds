package healthcareab.project.healthcare_booking_app.models;

public enum AppointmentStatus {
    BOOKED,      // Active appointment - patient has booked this slot
    CANCELLED,   // Cancelled by patient or employee
    COMPLETED    // Past appointment that was completed
}

