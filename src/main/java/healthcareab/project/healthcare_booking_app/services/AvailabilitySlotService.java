package healthcareab.project.healthcare_booking_app.services;

import healthcareab.project.healthcare_booking_app.dto.AvailabilitySlotRequest;
import healthcareab.project.healthcare_booking_app.dto.AvailabilitySlotResponse;
import healthcareab.project.healthcare_booking_app.models.Employee;
import healthcareab.project.healthcare_booking_app.models.Role;
import healthcareab.project.healthcare_booking_app.models.User;
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

    //-------Validation methods--------
    private Employee validateAndGetEmployee(User user) {
        if (!(user instanceof Employee)) {
            throw new IllegalArgumentException("Only employees can manage availability slots");
        }

        Employee employee = (Employee) user;

        if(!employee.getRoles().contains(Role.EMPLOYEE)){
            throw new IllegalArgumentException("User must have EMPLOYEE role");
        }

        if(!employee.isAvailableForBooking()){
            throw new IllegalArgumentException("Employee is not available for booking");
        }
        return employee;
    }
}
