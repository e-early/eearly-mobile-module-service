package si.result.eearly.domain.profile;

import jakarta.persistence.*;
import lombok.*;
import si.result.eearly.domain.device.DeviceUser;
import si.result.eearly.domain.schedule.Schedule;
import si.result.eearly.domain.token.MobileToken;

import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "profile")
@Getter
@Setter
@NoArgsConstructor
public class Profile {
    @Id
    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "picture", columnDefinition = "bytea")
    private byte[] picture;

    @Column(name = "notification_day_summary", nullable = false)
    private boolean notificationDaySummary = false;

    @Column(name = "notification_day_summary_time")
    private LocalTime notificationDaySummaryTime;

    @Column(name = "notification_measurement_due", nullable = false)
    private boolean notificationMeasurementDue = false;

    @Column(name = "notification_measurement_due_minutes")
    private Integer notificationMeasurementDueMinutes;

    @Column(name = "notification_no_internet", nullable = false)
    private boolean notificationNoInternet = true;

    @Column(name = "notification_no_bluetooth", nullable = false)
    private boolean notificationNoBluetooth = true;

    @OneToMany(mappedBy = "primaryKey.profile")
    @EqualsAndHashCode.Exclude
    private List<DeviceUser> deviceUserList;

    @ManyToMany(mappedBy = "profiles")
    private List<Schedule> scheduleList;

    @OneToMany(mappedBy = "profile")
    private List<MobileToken> mobileTokens;

    public static Profile createProfileFromId(String userId) {
        Profile profile = new Profile();
        profile.setUserId(UUID.fromString(userId));
        return profile;
    }

    public static Profile createProfileFromUpsertCommand(byte[] picture, UpsertProfileCommand command) {
        Profile profileToUpsert = new Profile();
        profileToUpsert.setUserId(UUID.fromString(command.userId()));

        profileToUpsert.setPicture(picture);

        profileToUpsert.setNotificationDaySummary(command.notificationDaySummary());
        if (command.notificationDaySummary()) {
            profileToUpsert.setNotificationDaySummaryTime(LocalTime.parse(command.notificationDaySummaryTimeString()));
        }

        profileToUpsert.setNotificationMeasurementDue(command.notificationMeasurementDue());
        if (command.notificationMeasurementDue()) {
            profileToUpsert.setNotificationMeasurementDueMinutes(command.notificationMeasurementDueMinutes());
        }

        profileToUpsert.setNotificationNoInternet(command.notificationNoInternet());
        profileToUpsert.setNotificationNoBluetooth(command.notificationNoBluetooth());

        return profileToUpsert;
    }

    public void apply(Profile patch) {
        if (patch.picture != null) {
            this.picture = patch.picture;
        }

        this.notificationDaySummary = patch.notificationDaySummary;
        this.notificationDaySummaryTime = patch.notificationDaySummary
                ? patch.notificationDaySummaryTime
                : null;

        this.notificationMeasurementDue = patch.notificationMeasurementDue;
        this.notificationMeasurementDueMinutes = patch.notificationMeasurementDue
                ? patch.notificationMeasurementDueMinutes
                : null;

        this.notificationNoInternet = patch.notificationNoInternet;
        this.notificationNoBluetooth = patch.notificationNoBluetooth;
    }
}
