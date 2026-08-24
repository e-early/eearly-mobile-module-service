package si.result.eearly.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import si.result.eearly.domain.device.DeviceUser;
import si.result.eearly.domain.device.DeviceUserId;

@Repository
public interface DeviceUserRepository extends JpaRepository<DeviceUser, DeviceUserId> {

    @Query("SELECT d FROM DeviceUser d WHERE d.primaryKey.profile.userId = ?1 AND d.primaryKey.device.id = ?2")
    Optional<DeviceUser> findByUserAndDevice(UUID userId, UUID deviceId);

    List<DeviceUser> findAllByPrimaryKeyProfileUserId(UUID userId);
}
