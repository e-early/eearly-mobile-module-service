package si.result.eearly.domain.apikey;


import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;
import si.result.eearly.domain.device.Device;
import si.result.eearly.domain.profile.Profile;

import java.util.UUID;

@Entity
@Table(name = "apikey")
@Data
@NoArgsConstructor
public class ApiKey {

  @Id
  @Column(name = "id")
  @UuidGenerator
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "device_id")
  private Device device;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private Profile profile;

  @Column(name = "key_value", nullable = false)
  private String value;

  public static ApiKey create(ApiKeyCommand command) {
    ApiKey apiKey = new ApiKey();
    apiKey.setDevice(Device.createDeviceFromId(command.deviceId()));
    apiKey.setProfile(Profile.createProfileFromId(command.userId()));
    apiKey.setValue(command.value());
    return apiKey;
  }

}


