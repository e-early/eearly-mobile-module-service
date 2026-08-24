package si.result.eearly.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import si.result.eearly.domain.schedule.ScheduleUser;
import si.result.eearly.domain.schedule.ScheduleUserId;

import java.util.UUID;

@Repository
public interface ScheduleUserRepository extends JpaRepository<ScheduleUser, ScheduleUserId> {

    ScheduleUser findByUserIdAndScheduleId(UUID userId, UUID scheduleId);

    @Modifying
    @Query("DELETE FROM ScheduleUser su WHERE su.scheduleId = :scheduleId")
    void deleteByScheduleId(@Param("scheduleId") UUID scheduleId);
}
