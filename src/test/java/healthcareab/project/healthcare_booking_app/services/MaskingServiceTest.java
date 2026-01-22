package healthcareab.project.healthcare_booking_app.services;

import healthcareab.project.healthcare_booking_app.dto.PatientResponse;
import healthcareab.project.healthcare_booking_app.models.Patient;
import healthcareab.project.healthcare_booking_app.models.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("MaskingService Unit Tests")
class MaskingServiceTest {

    private MaskingService maskingService;

    @BeforeEach
    void setUp() {
        maskingService = new MaskingService();
    }

    @Test
    @DisplayName("shouldMaskSingleName → 'John' → 'J***'")
    void shouldMaskSingleName() {
        String result = maskingService.maskName("John");
        assertEquals("J***", result);
    }

    @Test
    @DisplayName("shouldMaskFullName → 'John Doe' → 'J*** D**'")
    void shouldMaskFullName() {
        String result = maskingService.maskName("John Doe");
        assertEquals("J*** D**", result);
    }

    @Test
    @DisplayName("shouldMaskThreePartName → 'Anna Maria Berg' → 'A*** M*** B***'")
    void shouldMaskThreePartName() {
        String result = maskingService.maskName("Anna Maria Berg");
        assertEquals("A*** M*** B***", result);
    }

    @Test
    @DisplayName("shouldHandleNullName → null → '***'")
    void shouldHandleNullName() {
        String result = maskingService.maskName(null);
        assertEquals("***", result);
    }

    @Test
    @DisplayName("shouldMaskPatientDataObject")
    void shouldMaskPatientDataObject() {
        Patient patient = new Patient();
        patient.setFirstName("John");
        patient.setLastName("Doe");
        patient.setEmail("john@test.com");
        patient.setDateOfBirth(LocalDate.of(1990, 1, 1));
        patient.setRoles(Set.of(Role.PATIENT));

        PatientResponse response = maskingService.maskPatientData(patient);

        assertNotNull(response);
        assertEquals("J***", response.getFirstName());
        assertEquals("D**", response.getLastName());
        assertEquals("J*** D**", response.getFullName());
        assertEquals("john@test.com", response.getEmail());
    }
}