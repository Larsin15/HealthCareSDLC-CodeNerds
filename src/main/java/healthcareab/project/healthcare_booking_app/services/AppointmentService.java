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
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

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

    public AppointmentResponse cancelAppointment(UUID appointmentId, User currentUser) {
        // 1. Fetch the appointment
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new IllegalArgumentException("Appointment not found"));

        // 2. Check that appointment is still active
        if (appointment.getStatus() != AppointmentStatus.BOOKED) {
            throw new IllegalArgumentException("This appointment cannot be cancelled (status: "
                    + appointment.getStatus() + ")");
        }

        // 3. Validate ownership and cancellation rules
        boolean isPatient = currentUser instanceof Patient
                && appointment.getPatient().getId().equals(currentUser.getId());
        boolean isEmployee = currentUser instanceof Employee
                && appointment.getEmployee().getId().equals(currentUser.getId());

        if (!isPatient && !isEmployee) {
            throw new IllegalArgumentException("You can only cancel your own appointments");
        }

        // 4. If patient (check 24 hour rule)
        if (isPatient && !appointment.canBeCancelledByPatient()) {
            throw new IllegalArgumentException(
                    "Patients can only cancel appointments at least " + MIN_HOURS_BEFORE_PATIENT_CANCEL
                            + " hours before the scheduled time");
        }

        // 5. Cancel the appointment
        appointment.cancel();
        appointmentRepository.save(appointment);

        // 6. Return slot to AVAILABLE status
        AvailabilitySlot slot = appointment.getAvailabilitySlot();
        slot.setStatus(SlotStatus.AVAILABLE);
        availabilitySlotRepository.save(slot);

        // 7. Return response
        if (isPatient) {
            return AppointmentResponse.forPatient(appointment);
        } else {
            return AppointmentResponse.forEmployee(appointment);
        }
    }

    @Transactional(readOnly = true)
    public List<AppointmentResponse> getPatientAppointments(User currentUser) {
        Patient patient = validateAndGetPatient(currentUser);

        List<Appointment> appointments = appointmentRepository
                .findByPatientIdOrderBySlotStartTimeDesc(patient.getId());

        return appointments.stream()
                .map(AppointmentResponse::forPatient)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AppointmentResponse> getEmployeeAppointments(User currentUser) {
        Employee employee = validateAndGetEmployee(currentUser);

        List<Appointment> appointments = appointmentRepository
                .findByEmployeeIdOrderBySlotStartTimeDesc(employee.getId());

        return appointments.stream()
                .map(AppointmentResponse::forEmployee)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public AppointmentResponse getAppointmentById(UUID appointmentId, User currentUser) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new IllegalArgumentException("Appointment not found"));

        // Check authorization
        boolean isPatient = currentUser instanceof Patient
                && appointment.getPatient().getId().equals(currentUser.getId());
        boolean isEmployee = currentUser instanceof Employee
                && appointment.getEmployee().getId().equals(currentUser.getId());

        if (!isPatient && !isEmployee) {
            throw new IllegalArgumentException("You are not authorized to view this appointment");
        }

        if (isPatient) {
            return AppointmentResponse.forPatient(appointment);
        } else {
            return AppointmentResponse.forEmployee(appointment);
        }
    }

    // method helpers
    private Patient validateAndGetPatient(User user) {
        if (!(user instanceof Patient)) {
            throw new IllegalArgumentException("Only patients can book appointments");
        }
        return (Patient) user;
    }

    private Employee validateAndGetEmployee(User user) {
        if (!(user instanceof Employee)) {
            throw new IllegalArgumentException("Only employees can access employee appointments");
        }
        return (Employee) user;
    }
}
