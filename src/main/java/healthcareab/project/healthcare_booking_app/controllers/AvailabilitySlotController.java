package healthcareab.project.healthcare_booking_app.controllers;

import healthcareab.project.healthcare_booking_app.services.AuthService;
import healthcareab.project.healthcare_booking_app.services.AvailabilitySlotService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/availability")
public class AvailabilitySlotController {

    private final AvailabilitySlotService availabilitySlotService;
    private final AuthService authService;

    public AvailabilitySlotController(AvailabilitySlotService availabilitySlotService, AuthService authService) {
        this.availabilitySlotService = availabilitySlotService;
        this.authService = authService;
    }



}
