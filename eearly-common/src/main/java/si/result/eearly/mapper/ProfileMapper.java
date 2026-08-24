package si.result.eearly.mapper;

import org.mapstruct.*;
import si.result.eearly.domain.profile.Profile;
import si.result.eearly.domain.profile.UpdateUsernameCommand;
import si.result.eearly.genproto.UpsertProfileRequest;
import si.result.eearly.genproto.UpsertProfileResponse;
import si.result.eearly.domain.profile.UpsertProfileCommand;

import java.nio.charset.StandardCharsets;

@Mapper(nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public interface ProfileMapper {


    default UpsertProfileCommand toUpsertCommand(UpsertProfileRequest request, String userId) {
        var upsertProfileCommand = new UpsertProfileCommand(
                userId,
                request.getPicture().toByteArray(),
                request.getNotificationDaySummary(),
                request.getNotificationDaySummaryTime(),
                request.getNotificationMeasurementDue(),
                request.getNotificationMeasurementDueMinutes(),
                request.getNotificationNoInternet(),
                request.getNotificationNoBluetooth());
        return upsertProfileCommand;

    }

    UpsertProfileResponse toUpsertProfileResponse(Profile profile);

    @Mapping(source = "username", target = "username")
    @Mapping(source = "userId", target = "userId")
    UpdateUsernameCommand toUpdateUsernameCommand(String userId, String username);

    @Mapping(source = "profile.userId", target = "userId")
    @Mapping(source = "username", target = "username")
    @Mapping(source = "profile.picture", target = "picture", qualifiedByName = "byteArrayToString")
    @Mapping(source = "profile.notificationDaySummary", target = "notificationDaySummary")
    @Mapping(source = "profile.notificationDaySummaryTime", target = "notificationDaySummaryTime")
    @Mapping(source = "profile.notificationMeasurementDue", target = "notificationMeasurementDue")
    @Mapping(source = "profile.notificationMeasurementDueMinutes", target = "notificationMeasurementDueMinutes")
    @Mapping(source = "profile.notificationNoInternet", target = "notificationNoInternet")
    @Mapping(source = "profile.notificationNoBluetooth", target = "notificationNoBluetooth")
    si.result.eearly.genproto.Profile toGrpcProfile(Profile profile, String username);

    @Named("byteArrayToString")
    default String byteArrayToString(byte[] bytes) {
        return new String(bytes, StandardCharsets.UTF_8);
    }
}
