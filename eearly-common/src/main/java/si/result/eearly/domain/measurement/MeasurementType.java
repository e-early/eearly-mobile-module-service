package si.result.eearly.domain.measurement;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;
import si.result.eearly.domain.device.Device;
import si.result.eearly.domain.enums.MeasurementSubTypeEnum;
import si.result.eearly.domain.enums.MeasurementTypeEnum;
import si.result.eearly.domain.schedule.ScheduleEntry;

import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "measurement_type")
@Getter
@Setter
@NoArgsConstructor
public class MeasurementType {

    @Id
    @Column(name = "id")
    @UuidGenerator
    private UUID id;

    @Column(name = "measurement_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private MeasurementTypeEnum measurementType;

    @Column(name = "measurement_sub_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private MeasurementSubTypeEnum measurementSubType;

    @Column(name = "min_value", nullable = false)
    private double minValue;

    @Column(name = "max_value", nullable = false)
    private double maxValue;

    @Column(name = "unit", nullable = false)
    private String unit;

    @Column(name = "ehr_observation_id", nullable = false)
    private String observationId;

    @ManyToMany(mappedBy = "measurementTypeList")
    @EqualsAndHashCode.Exclude
    private List<Device> deviceList;

    @OneToMany(mappedBy = "measurementType")
    private List<ScheduleEntry> scheduleEntryList;

    public static MeasurementType createMeasurementTypeForId(String measurementTypeId) {
        MeasurementType measurementType = new MeasurementType();
        measurementType.setId(UUID.fromString(measurementTypeId));
        return measurementType;
    }
}
