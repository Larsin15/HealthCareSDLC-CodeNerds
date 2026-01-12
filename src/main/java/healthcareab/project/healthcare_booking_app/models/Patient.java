package healthcareab.project.healthcare_booking_app.models;

import jakarta.persistence.*;

@Entity
@DiscriminatorValue("PATIENT")
public class Patient extends User {

    public Patient() {
        super();
    }

    public Patient(String username, String password, String email, String firstName, String lastName, String address) {
        super(username, password, email, firstName, lastName, address);
    }

}
