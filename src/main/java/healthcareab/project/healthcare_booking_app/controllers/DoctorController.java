package healthcareab.project.healthcare_booking_app.controllers;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/doctor")
public class DoctorController {

    @PreAuthorize("hasAnyRole('DOCTOR','ADMIN')")
    @GetMapping("/dashboard")
    public String doctorDashboard() {
        return "Doctor dashboard";
    }
}
