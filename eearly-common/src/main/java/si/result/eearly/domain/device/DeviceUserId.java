package si.result.eearly.domain.device;

import jakarta.persistence.Embeddable;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import si.result.eearly.domain.profile.Profile;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeviceUserId implements Serializable {
    @ManyToOne
    private Device device;

    @ManyToOne
    private Profile profile;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DeviceUserId that)) return false;
        return Objects.equals(device.getId(), that.device.getId()) && Objects.equals(profile.getUserId(), that.profile.getUserId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(device, profile);
    }
}
