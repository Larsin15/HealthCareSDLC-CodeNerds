import healthcareab.project.healthcare_booking_app.models.User;

import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorColumn("EMPLOYEE")
public class Employee extends User {

}