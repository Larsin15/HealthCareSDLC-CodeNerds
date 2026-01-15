package healthcareab.project.healthcare_booking_app.services;

import healthcareab.project.healthcare_booking_app.repository.AvailabilitySlotRepository;
import healthcareab.project.healthcare_booking_app.repository.EmployeeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AvailabilitySlotService {

    private final AvailabilitySlotRepository availabilitySlotRepository;
    private final EmployeeRepository employeeRepository;
    private final AuthService authService;

    // Business rules constants
    private static final int WORKING_HOUR_START = 8;
    private static final int WORKING_HOUR_END = 16;
    private static final int SLOT_DURATION_MINUTES = 30;

    public AvailabilitySlotService(AvailabilitySlotRepository availabilitySlotRepository, EmployeeRepository employeeRepository, AuthService authService) {
        this.availabilitySlotRepository = availabilitySlotRepository;
        this.employeeRepository = employeeRepository;
        this.authService = authService;
    }
}
