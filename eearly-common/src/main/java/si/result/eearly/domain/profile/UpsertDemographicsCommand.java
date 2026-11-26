package si.result.eearly.domain.profile;

import java.time.LocalDate;
import si.result.eearly.domain.enums.Sex;

/**
 * Demographics of one patient. Personal data: {@link #toString()} deliberately leaves the date of
 * birth and sex out so that the command can never leak them into a log line.
 */
public record UpsertDemographicsCommand(
        String userId,
        LocalDate dateOfBirth,
        Sex sex
) {

    @Override
    public String toString() {
        return "UpsertDemographicsCommand[userId=" + userId + "]";
    }
}
