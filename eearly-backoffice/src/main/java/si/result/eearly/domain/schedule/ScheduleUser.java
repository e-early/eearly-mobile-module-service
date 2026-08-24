package si.result.eearly.domain.schedule;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@IdClass(ScheduleUserId.class)
@Table(name = "schedule_user")
@Data
@NoArgsConstructor
public class ScheduleUser {

    @Id
    @Column(name = "user_id")
    UUID userId;

    @Id
    @Column(name = "schedule_id")
    UUID scheduleId;

    public ScheduleUser(String userId, String scheduleId) {
        this.userId = UUID.fromString(userId);
        this.scheduleId = UUID.fromString(scheduleId);
    }
}
