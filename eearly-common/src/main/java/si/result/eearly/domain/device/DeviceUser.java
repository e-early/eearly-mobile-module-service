package si.result.eearly.domain.device;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import si.result.eearly.domain.profile.Profile;

@Entity
@Table(name = "device_user")
@AssociationOverrides({
    @AssociationOverride(name = "primaryKey.device", joinColumns = @JoinColumn(name = "device_id")),
    @AssociationOverride(name = "primaryKey.profile", joinColumns = @JoinColumn(name = "profile_user_id")),
})
@Data
@NoArgsConstructor
public class DeviceUser {
  @EmbeddedId
  private DeviceUserId primaryKey;

  @Column(name = "api_key", nullable = false)
  private String apiKey;

  @Transient
  public Profile getProfile() {
    return getPrimaryKey().getProfile();
  }

  public void setProfile(Profile profile) {
    getPrimaryKey().setProfile(profile);
  }

  @Transient
  public Device getDevice() {
    return getPrimaryKey().getDevice();
  }

  public void setDevice(Device device) {
    getPrimaryKey().setDevice(device);
  }

  public static DeviceUser create(String apiKey, Device device, Profile profile) {

    DeviceUserId deviceUserId = new DeviceUserId();
    deviceUserId.setDevice(device);
    deviceUserId.setProfile(profile);

    DeviceUser deviceUser = new DeviceUser();
    deviceUser.setPrimaryKey(deviceUserId);
    deviceUser.setApiKey(apiKey);

    return deviceUser;
  }
}
