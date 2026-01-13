package healthcareab.project.healthcare_booking_app.models;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

import java.time.LocalDate;

@Entity
@DiscriminatorValue("PATIENT")
public class Patient extends User {

    @Column(name = "phone_number", unique = true, nullable = true, length = 10)
    /*@Pattern(
            regexp = "^(\\+46|0)7[0-9]{8}$",
            message = "Ogiltigt svenskt mobilnummer")*/
    private String phoneNumber;

    @Column(name = "date_of_birth")
    /*@Pattern(regexp = "^\\d{8}$",
            message = "Date of Birth must be in the format YYYY-MM-DD")*/
    private LocalDate dateOfBirth; // In development, no last 4 digits validation yet

    public Patient() {
        super();
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

        if (dateOfBirth == null) {
            throw new IllegalArgumentException("Patient must have a date of birth");
        }

        if (dateOfBirth.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Date of birth cannot be in the future");
        }

    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }
}
