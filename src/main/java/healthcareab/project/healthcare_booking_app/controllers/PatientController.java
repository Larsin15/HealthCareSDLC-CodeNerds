package healthcareab.project.healthcare_booking_app.controllers;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/patient")
public class PatientController {

    @PreAuthorize("hasRole('PATIENT')")
    @GetMapping("/profile")
    public String getMyProfile() {
        return "Patient profile";
    }
}
