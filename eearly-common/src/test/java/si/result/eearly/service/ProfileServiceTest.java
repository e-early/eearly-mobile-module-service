package si.result.eearly.service;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import si.result.eearly.domain.exception.ErrorCode;
import si.result.eearly.domain.exception.ValidationException;
import si.result.eearly.domain.profile.UpsertProfileCommand;

import java.util.UUID;
import si.result.eearly.ehr.EhrbaseClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;

@ExtendWith(MockitoExtension.class)
public class ProfileServiceTest {

    @InjectMocks
    private ProfileServiceImpl profileService;

    @Mock
    private EhrbaseClient ehrbaseClient;

    @Test
    void invalidNotificationDaySummaryRequest() {

        var command = new UpsertProfileCommand(
                UUID.randomUUID().toString(),
                null,
                true,
                null,
                false,
                0,
                false,
                false
        );

        var e = catchThrowableOfType(
                () -> profileService.upsertProfile(command),
                ValidationException.class);

        assertThat(e).isNotNull();
        assertThat(e.getErrorCode()).isEqualTo(ErrorCode.VALIDATION_ERROR);
        assertThat(e.getMessage()).isEqualTo("notificationDaySummaryTime must be set when notificationDaySummary is TRUE");
    }

    @Test
    void invalidTimeNotificationDaySummaryRequest() {

        var command = new UpsertProfileCommand(
                UUID.randomUUID().toString(),
                null,
                true,
                "59:00",
                false,
                0,
                false,
                false
        );

        var e = catchThrowableOfType(
                () -> profileService.upsertProfile(command),
                ValidationException.class);

        assertThat(e).isNotNull();
        assertThat(e.getErrorCode()).isEqualTo(ErrorCode.VALIDATION_ERROR);
        assertThat(e.getMessage()).isEqualTo("notificationDaySummaryTime is not a valid time (HH:mm:ss)");
    }

    @Test
    void invalidNotificationMeasurementDueRequest() {

        var command = new UpsertProfileCommand(
                UUID.randomUUID().toString(),
                null,
                false,
                null,
                true,
                0,
                false,
                false
        );

        var e = catchThrowableOfType(
                () -> profileService.upsertProfile(command),
                ValidationException.class);

        assertThat(e).isNotNull();
        assertThat(e.getErrorCode()).isEqualTo(ErrorCode.VALIDATION_ERROR);
        assertThat(e.getMessage()).isEqualTo("notificationMeasurementDueMinutes must be set when notificationMeasurementDue is TRUE");
    }
}
