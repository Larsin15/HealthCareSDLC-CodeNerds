package healthcareab.project.healthcare_booking_app.services;

import healthcareab.project.healthcare_booking_app.repository.AppointmentRepository;
import healthcareab.project.healthcare_booking_app.repository.AvailabilitySlotRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
}
