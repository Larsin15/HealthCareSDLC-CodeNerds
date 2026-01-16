package healthcareab.project.healthcare_booking_app.services;

import healthcareab.project.healthcare_booking_app.dto.AvailabilitySlotRequest;
import healthcareab.project.healthcare_booking_app.dto.AvailabilitySlotResponse;
import healthcareab.project.healthcare_booking_app.models.*;
import healthcareab.project.healthcare_booking_app.repository.AvailabilitySlotRepository;
import healthcareab.project.healthcare_booking_app.repository.EmployeeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

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

    //Create a new availability slot for the current employee
    public AvailabilitySlotResponse createSlot(AvailabilitySlotRequest request, User currentUser) {
        // Ensure user is an employee
        Employee employee = validateAndGetEmployee(currentUser);

        // Validate business rules
        validateSlotTimes(request.getStartTime(), request.getEndTime());
        validateNoOverlap(employee, request.getStartTime(), request.getEndTime(), null);

        // Create and save slot
        AvailabilitySlot slot = new AvailabilitySlot(employee, request.getStartTime(), request.getEndTime());
        slot.setStatus(SlotStatus.AVAILABLE);
        slot = availabilitySlotRepository.save(slot);

        return mapToResponse(slot);
    }

    //Get all slots for the current employee
    @Transactional(readOnly = true)
    public List<AvailabilitySlotResponse> getMySlots(User currentUser) {
        Employee employee = validateAndGetEmployee(currentUser);
        List<AvailabilitySlot> slots = availabilitySlotRepository.findEByEmployee(employee);
        return slots.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    //Get all available slots for patients to see
    @Transactional(readOnly = true)
    public List<AvailabilitySlotResponse> getAvailableSlots(ZonedDateTime start, ZonedDateTime end) {
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("UTC"));
        ZonedDateTime startDate = (start != null) ? start : now;
        ZonedDateTime endDate = (end != null) ? end : now.plusMonths(3); //Default 3 months ahead

        List<AvailabilitySlot> slots = availabilitySlotRepository
                .findByStatusAndStartTimeGreaterThanEqualAndEndTimeLessThanEqualOrderByStartTimeAsc(
                        SlotStatus.AVAILABLE, startDate, endDate);

        return slots.stream()
                .filter(slot -> slot.getEmployee().isAvailableForBooking())
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    //Get available slots for a specific employee
    @Transactional(readOnly = true)
    public List<AvailabilitySlotResponse> getAvailableSlotsByEmployee(
            UUID employeeId, ZonedDateTime start, ZonedDateTime end) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found"));

        if (!employee.isAvailableForBooking()) {
            throw new IllegalArgumentException("Employee is not available for booking");
        }

        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("UTC"));
        ZonedDateTime startDate = (start != null) ? start : now;
        ZonedDateTime endDate = (end != null) ? end : now.plusMonths(3);

        List<AvailabilitySlot> slots = availabilitySlotRepository
                .findByEmployeeAndStatusAndStartTimeGreaterThanEqualAndEndTimeLessThanEqualOrderByStartTimeAsc(
                        employee, SlotStatus.AVAILABLE, startDate, endDate);

        return slots.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    //update an existing available slots
    public AvailabilitySlotResponse updateSlot(UUID slotId, AvailabilitySlotRequest request, User currentUser) {
        Employee employee = validateAndGetEmployee(currentUser);

        AvailabilitySlot slot = availabilitySlotRepository.findById(slotId)
                .orElseThrow(() -> new IllegalArgumentException("Slot not found"));

        // Verify ownership for slot
        if (!slot.getEmployee().getId().equals(employee.getId())) {
            throw new IllegalArgumentException("You can only update your own slots");
        }

        // Cannot update booked or completed slots
        if (slot.getStatus() == SlotStatus.BOOKED || slot.getStatus() == SlotStatus.COMPLETED) {
            throw new IllegalArgumentException("Cannot update " + slot.getStatus() + " slots");
        }

        // Validate new times
        validateSlotTimes(request.getStartTime(), request.getEndTime());
        validateNoOverlap(employee, request.getStartTime(), request.getEndTime(), slotId);

        // Update slot
        slot.setStartTime(request.getStartTime());
        slot.setEndTime(request.getEndTime());
        slot = availabilitySlotRepository.save(slot);

        return mapToResponse(slot);
    }

    //Delete a available slot
    public void cancelSlot(UUID slotId, User currentUser) {
        Employee employee = validateAndGetEmployee(currentUser);

        AvailabilitySlot slot = availabilitySlotRepository.findById(slotId)
                .orElseThrow(() -> new IllegalArgumentException("Slot not found"));

        // Verify ownership of slot
        if (!slot.getEmployee().getId().equals(employee.getId())) {
            throw new IllegalArgumentException("You can only cancel your own slots");
        }

        // Cannot cancel completed slots
        if (slot.getStatus() == SlotStatus.COMPLETED) {
            throw new IllegalArgumentException("Cannot cancel completed slots");
        }

        // If booked, set to cancelled. Otherwise, delete
        if (slot.getStatus() == SlotStatus.BOOKED) {
            slot.setStatus(SlotStatus.CANCELLED);
            availabilitySlotRepository.save(slot);
        } else {
            availabilitySlotRepository.delete(slot);
        }
    }












    //-------Validation methods--------
    //Validate if the user is an employee
    private Employee validateAndGetEmployee(User user) {
        if (!(user instanceof Employee)) {
            throw new IllegalArgumentException("Only employees can manage availability slots");
        }

        Employee employee = (Employee) user;

        if (!employee.getRoles().contains(Role.EMPLOYEE)) {
            throw new IllegalArgumentException("User must have EMPLOYEE role");
        }

        if (!employee.isAvailableForBooking()) {
            throw new IllegalArgumentException("Employee is not available for booking");
        }
        return employee;
    }

    //Validate slot times according to buisness rules.
    private void validateSlotTimes(ZonedDateTime starTime, ZonedDateTime endTime) {
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("UTC"));

        //Future only
        if (starTime.isBefore(now)) {
            throw new IllegalArgumentException("Can not create slots in the past");
        }

        //weekdays validation
        DayOfWeek dayOfWeek = starTime.getDayOfWeek();
        if (dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY) {
            throw new IllegalArgumentException("Slots can only be created on weekdays (Monday - Friday)");
        }
        //Working hours validation (8-16)
        int startHour = starTime.getHour();
        int startMinute = starTime.getMinute();
        int endHour = endTime.getHour();
        int endMinute = endTime.getMinute();

        //Start time must be on the hour of half-hour
        if (startMinute != 0 && endMinute != 30) {
            throw new IllegalArgumentException(
                    String.format("Slots must be within working hours (%02d:00-%02d:00)", WORKING_HOUR_START, WORKING_HOUR_END)
            );
        }
        // Check working hours
        if (startHour < WORKING_HOUR_START ||
                (startHour == WORKING_HOUR_START && startMinute < 0) ||
                startHour >= WORKING_HOUR_END ||
                (startHour == WORKING_HOUR_END && startMinute > 0)) {
            throw new IllegalArgumentException(
                    String.format("Slots must be within working hours (%02d:00-%02d:00)",
                            WORKING_HOUR_START, WORKING_HOUR_END));
        }
        // End time must be within working hours
        if (endHour > WORKING_HOUR_END ||
                (endHour == WORKING_HOUR_END && endMinute > 0)) {
            throw new IllegalArgumentException(
                    String.format("Slot end time must be within working hours (before %02d:00)",
                            WORKING_HOUR_END));
        }

        //Duration validation (exactly 30 minutes)
        Duration duration = Duration.between(starTime, endTime);
        long durationMinutes = duration.toMinutes();
        if (durationMinutes != SLOT_DURATION_MINUTES) {
            throw new IllegalArgumentException(String.format("Slot duration must be exactly %d minutes", SLOT_DURATION_MINUTES));
        }
    }

    //Validate that the new slot does not overlap with existing slots.
    private void validateNoOverlap(Employee employee, ZonedDateTime startTime, ZonedDateTime endTime, UUID excludeSlotId) {

        //Check for overlaping slots for better error messages
        boolean hasOverlap = availabilitySlotRepository.hasOverlappingSlot(
                employee, startTime, endTime, Arrays.asList(SlotStatus.CANCELLED));
        if (hasOverlap) {
            // Get overlapping slots for better error message
            List<AvailabilitySlot> overlappingSlots = availabilitySlotRepository.findOverlappingSlots(
                    employee, startTime, endTime);

            // Filter out the slot being updated
            if (excludeSlotId != null) {
                overlappingSlots = overlappingSlots.stream()
                        .filter(slot -> !slot.getId().equals(excludeSlotId))
                        .collect(Collectors.toList());
            }

            if (!overlappingSlots.isEmpty()) {
                AvailabilitySlot conflict = overlappingSlots.get(0);
                throw new IllegalArgumentException(
                        String.format("Slot overlaps with existing slot: %s to %s",
                                conflict.getStartTime(), conflict.getEndTime()));
            }
        }
    }

    //---------Mapping methods-------------
    //Map availanilityslot entity to availabilityslotresponse dto
    private AvailabilitySlotResponse mapToResponse(AvailabilitySlot slot) {
        Employee employee = slot.getEmployee();
        return new AvailabilitySlotResponse(
                slot.getId(),
                employee.getId(),
                employee.getFirstName(),
                employee.getSpecialization(),
                slot.getStartTime(),
                slot.getEndTime(),
                slot.getStatus()

        );
    }
}
