package si.result.eearly.domain.profile;

import java.time.LocalDate;
import si.result.eearly.domain.enums.Sex;

/**
 * Demographics of one patient. Personal data: {@link #toString()} deliberately leaves the date of
 * birth and sex out so that the command can never leak them into a log line.
 *
 * @param language ISO 639-1 code ({@link ProfileLanguage}); null keeps the profile's current one
 */
public record UpsertDemographicsCommand(
        String userId,
        LocalDate dateOfBirth,
        Sex sex,
        String language
) {

    @Override
    public String toString() {
        return "UpsertDemographicsCommand[userId=" + userId + "]";
    }
}
