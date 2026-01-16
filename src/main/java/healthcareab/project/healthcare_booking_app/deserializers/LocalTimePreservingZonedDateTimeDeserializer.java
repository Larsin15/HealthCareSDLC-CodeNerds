package healthcareab.project.healthcare_booking_app.deserializers;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;


//Custom deserializer that preserves the local time component from JSON.
public class LocalTimePreservingZonedDateTimeDeserializer extends JsonDeserializer<ZonedDateTime> {

    private static final ZoneId SWEDISH_TIMEZONE = ZoneId.of("Europe/Stockholm");
    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

    @Override
    public ZonedDateTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String dateTimeString = p.getText();

        if (dateTimeString == null || dateTimeString.isEmpty()) {
            return null;
        }

        try {
            // Parse the string to get the local date/time component
            // This preserves the hour/minute the user entered
            ZonedDateTime parsed = ZonedDateTime.parse(dateTimeString, ISO_FORMATTER);

            // Extract the local date/time and apply Swedish timezone
            // This ensures "14:00:00+01:00" becomes "14:00:00+01:00" (Swedish), not "13:00:00Z"
            LocalDateTime localDateTime = parsed.toLocalDateTime();
            return localDateTime.atZone(SWEDISH_TIMEZONE);

        } catch (DateTimeParseException e) {
            throw new IOException("Invalid date format: " + dateTimeString, e);
        }
    }
}
