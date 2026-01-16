package healthcareab.project.healthcare_booking_app.repository;

import healthcareab.project.healthcare_booking_app.models.AvailabilitySlot;
import healthcareab.project.healthcare_booking_app.models.Employee;
import healthcareab.project.healthcare_booking_app.models.SlotStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AvailabilitySlotRepository extends JpaRepository<AvailabilitySlot, UUID> {


    //Find all slots for a specific employee
    List<AvailabilitySlot> findEByEmployee (Employee employee);


    //Find all slots with a specific status
    List<AvailabilitySlot> findByStatus(SlotStatus status);

    //Find all slots for an employee with a specific status
    List<AvailabilitySlot> findByEmployeeAndStatus(Employee employee, SlotStatus status);

    //Find all slots for an employee within a date range
    List<AvailabilitySlot> findByEmployeeAndStartTimeGreaterThanEqualAndEndTimeLessThanEqualOrderByStartTimeAsc(
            Employee employee,
            ZonedDateTime startTime,
            ZonedDateTime endTime);

    //Find all Available slots within a date range
    List<AvailabilitySlot> findByStatusAndStartTimeGreaterThanEqualAndEndTimeLessThanEqualOrderByStartTimeAsc(
            SlotStatus status,
            ZonedDateTime startTime,
            ZonedDateTime endTime
    );

    //Find availanle slots for a specific employee within a date range
    List<AvailabilitySlot> findByEmployeeAndStatusAndStartTimeGreaterThanEqualAndEndTimeLessThanEqualOrderByStartTimeAsc(
            Employee employee,
            SlotStatus status,
            ZonedDateTime startTime,
            ZonedDateTime endTime
    );

    //---------Overlap detection---------


    //Check if an employee has any overlapping slots in a time range
    @Query("SELECT COUNT(s) > 0 FROM AvailabilitySlot s " +
            "WHERE s.employee = :employee " +
            "AND s.startTime < :endTime " +
            "AND s.endTime > :startTime " +
            "AND s.status NOT IN :excludeStatuses")
    boolean hasOverlappingSlot(
            @Param("employee") Employee employee,
            @Param("startTime") ZonedDateTime startTime,
            @Param("endTime") ZonedDateTime endTime,
            @Param("excludeStatuses") List<SlotStatus> excludeStatuses
    );

    //Find all overlapping slots for an employee in a time range
    @Query("SELECT s FROM AvailabilitySlot s " +
            "WHERE s.employee = :employee " +
            "AND s.startTime < :endTime " +
            "AND s.endTime > :startTime " +
            "AND s.status IN (" +
            "  healthcareab.project.healthcare_booking_app.models.SlotStatus.AVAILABLE, " +
            "  healthcareab.project.healthcare_booking_app.models.SlotStatus.BOOKED" +
            ")")
    List<AvailabilitySlot> findOverlappingSlots(
            @Param("employee") Employee employee,
            @Param("startTime") ZonedDateTime startTime,
            @Param("endTime") ZonedDateTime endTime
    );


    //------------------Future slots-------------------
    //Find future slots for an employee(startTime after now)
    List<AvailabilitySlot> findByEmployeeAndStartTimeAfterOrderByStartTimeAsc(
            Employee employee,
            ZonedDateTime now
    );

    //Find all future available slots for an employee
    List<AvailabilitySlot> findByEmployeeAndStatusAndStartTimeAfterOrderByStartTimeAsc(
            Employee employee,
            SlotStatus status,
            ZonedDateTime now
    );

    //------------ Slot queries-----------------
    //Find a specific slot by employee and exact time
    Optional<AvailabilitySlot> findByEmployeeAndStartTime(
            Employee employee,
            ZonedDateTime startTime
    );


    //Count available slots for an employee
    long countByEmployeeAndStatus(Employee employee, SlotStatus status);
}
