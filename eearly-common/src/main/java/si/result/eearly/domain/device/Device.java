package si.result.eearly.domain.device;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import si.result.eearly.domain.manufacturer.Manufacturer;
import si.result.eearly.domain.measurement.MeasurementType;

import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "device")
@Getter
@Setter
@NoArgsConstructor
public final class Device {

    @Id
    @Column(name = "id")
    @UuidGenerator
    private UUID id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "picture", columnDefinition = "bytea")
    private byte[] picture;

    @Column(name = "description", nullable = false)
    private String description;

    @Column(name = "connection_type", nullable = false)
    private String connectionType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manufacturer_id", nullable = false)
    private Manufacturer manufacturer;

    @Column(name = "bluetooth_device_names")
    private List<String> bluetoothDeviceNames;

    @OneToMany(mappedBy = "primaryKey.device")
    @EqualsAndHashCode.Exclude
    private List<DeviceUser> deviceUserList;

    @ManyToMany(cascade = CascadeType.ALL)
    @JoinTable(
        name = "device_measurement_type",
        joinColumns = @JoinColumn(name = "device_id", referencedColumnName = "id"),
        inverseJoinColumns = @JoinColumn(name = "measurement_type_id", referencedColumnName = "id")
    )
    @EqualsAndHashCode.Exclude
    private List<MeasurementType> measurementTypeList;

    public static Device createDeviceFromId(String deviceId) {
        Device device = new Device();
        device.id = UUID.fromString(deviceId);
        return device;
    }

}
