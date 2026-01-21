package healthcareab.project.healthcare_booking_app.services;


import healthcareab.project.healthcare_booking_app.models.Patient;
import org.springframework.stereotype.Service;

@Service
public class MaskingService {

    /**
     * Masks a name (single or full) to GDPR-compliant format.
     *
     * Rules:
     * - Split by whitespace.
     * - Each part becomes: first letter + N asterisks
     *   where N = min(3, word.length() - 1)
     *
     * Examples:
     * - "John" -> "J***"
     * - "John Doe" -> "J*** D**"
     * - "Anna Maria Berg" -> "A*** M*** B***"
     * - null / blank -> "***"
     */
    public String maskName(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) {
            return "***";
        }

        String[] parts = fullName.trim().split("\\s+");
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < parts.length; i++) {
            if (i > 0) {
                result.append(" ");
            }
            result.append(maskWord(parts[i]));
        }

        return result.toString();
    }

    private String maskWord(String word) {
        if (word == null || word.isBlank()) {
            return "***";
        }
        String trimmed = word.trim();
        char first = trimmed.charAt(0);
        int stars = Math.min(3, Math.max(1, trimmed.length() - 1));
        return first + "*".repeat(stars);
    }


}