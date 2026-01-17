package healthcareab.project.healthcare_booking_app.converters;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@Converter(autoApply = false)
public class SwedishTimeConverter implements AttributeConverter<ZonedDateTime, LocalDateTime> {

    private static final ZoneId SWEDISH_TIMEZONE = ZoneId.of("Europe/Stockholm");

    //Convert ZonedDateTime to LocalDateTime for database storage.
    @Override
    public LocalDateTime convertToDatabaseColumn(ZonedDateTime zonedDateTime) {
        if (zonedDateTime == null) {
            return null;
        }
        // Convert to Swedish timezone first, then extract LocalDateTime
        // This preserves the hour/minute values the user entered
        return zonedDateTime.toLocalDateTime();
    }

    //Convert LocalDateTime from database back to ZonedDateTime.
    @Override
    public ZonedDateTime convertToEntityAttribute(LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return null;
        }
        // Treat the LocalDateTime as Swedish time
        return localDateTime.atZone(SWEDISH_TIMEZONE);
    }
}
