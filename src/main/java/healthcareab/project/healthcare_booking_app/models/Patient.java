package healthcareab.project.healthcare_booking_app.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.Pattern;

import java.util.Set;

@Entity
@DiscriminatorValue("PATIENT")
public class Patient extends User {

    @Column(name = "phone_number", unique = true, nullable = false, length = 10)
    @Pattern(
            regexp = "^(\\+46|0)7[0-9]{8}$",
            message = "Ogiltigt svenskt mobilnummer"
    )
    private String phoneNumber;

    @Column(name = "date_of_birth")
    @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$",
            message = "Date of Birth must be in the format YYYY-MM-DD"
    )
    private String dateOfBirth; // In development, no last 4 digits validation yet

    public Patient() {
        super();
    }

    public Patient(String phoneNumber, String dateOfBirth) {
        this.phoneNumber = phoneNumber;
        this.dateOfBirth = dateOfBirth;
    }

    public Patient(String username, String password, Set<Role> roles, String phoneNumber, String dateOfBirth) {
        super(username, password, roles);
        this.phoneNumber = phoneNumber;
        this.dateOfBirth = dateOfBirth;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    @Override
    protected void onBeforeCreate() {
        super.onBeforeCreate(); // Call parent hook first

        if (getRoles() == null || getRoles().isEmpty()) {
            setRoles(java.util.Set.of(Role.PATIENT));
                java.util.Set<Role> roles = new java.util.HashSet<>(getRoles());
        } else if (!getRoles().contains(Role.PATIENT)) {
            java.util.Set<Role> roles = new java.util.HashSet<>(getRoles());
            roles.add(Role.PATIENT);
            setRoles(roles);
        }
    }

    @Override
    public void validateSpecificRules() {

    }
}
