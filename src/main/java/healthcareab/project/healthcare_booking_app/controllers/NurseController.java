package healthcareab.project.healthcare_booking_app.controllers;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/nurse")
@PreAuthorize("hasRole('NURSE')")
public class NurseController {

    @GetMapping("/tasks")
    public String nurseTasks() {
        return "Nurse tasks";
    }
}

