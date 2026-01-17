package healthcareab.project.healthcare_booking_app.services;

import healthcareab.project.healthcare_booking_app.dto.AppointmentRequest;
import healthcareab.project.healthcare_booking_app.dto.AppointmentResponse;
import healthcareab.project.healthcare_booking_app.models.*;
import healthcareab.project.healthcare_booking_app.repository.AppointmentRepository;
import healthcareab.project.healthcare_booking_app.repository.AvailabilitySlotRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;
import java.time.ZonedDateTime;

@Service
@Transactional
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final AvailabilitySlotRepository availabilitySlotRepository;

    private static final int MIN_HOURS_BEFORE_BOOKING = 1;
    private static final int MIN_HOURS_BEFORE_PATIENT_CANCEL = 24;

    public AppointmentService(
            AppointmentRepository appointmentRepository,
            AvailabilitySlotRepository availabilitySlotRepository
    ) {
        this.appointmentRepository = appointmentRepository;
        this.availabilitySlotRepository = availabilitySlotRepository;
    }

    public AppointmentResponse bookAppointment(AppointmentRequest request, User currentUser) {
        // 1. Validate user is a Patient
        Patient patient = validateAndGetPatient(currentUser);

        // 2. Check patient has no active bookings (max 1 active booking rule)
        if (appointmentRepository.hasActiveBooking(patient.getId())) {
            throw new IllegalArgumentException(
                    "You already have an active booking. Please cancel it before booking a new appointment.");
        }

        // 3. Fetch and validate the availability slot
        AvailabilitySlot slot = availabilitySlotRepository.findById(request.getAvailabilitySlotId())
                .orElseThrow(() -> new IllegalArgumentException("Availability slot not found"));

        // 4. Check slot is available
        if (slot.getStatus() != SlotStatus.AVAILABLE) {
            throw new IllegalArgumentException("This slot is no longer available");
        }

        // 5. Check booking time constraint (at least 1 hour before slot start)
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("UTC"));
        if (slot.getStartTime().isBefore(now.plusHours(MIN_HOURS_BEFORE_BOOKING))) {
            throw new IllegalArgumentException(
                    "Cannot book an appointment less than " + MIN_HOURS_BEFORE_BOOKING
                            + " hour(s) before the start time");
        }

        // 6. Check employee is available for booking
        Employee employee = slot.getEmployee();
        if (!employee.isAvailableForBooking()) {
            throw new IllegalArgumentException("The healthcare provider is not available for booking");
        }

        // 7. Create the appointment
        Appointment appointment = new Appointment(slot, patient, employee);
        if (request.getNotes() != null && !request.getNotes().isBlank()) {
            appointment.setNotes(request.getNotes());
        }

        // 8. Update slot status to BOOKED
        slot.setStatus(SlotStatus.BOOKED);
        availabilitySlotRepository.save(slot);

        // 9. Save and return appointment
        Appointment saved = appointmentRepository.save(appointment);
        return AppointmentResponse.forPatient(saved);
    }
}
