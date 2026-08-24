package si.result.eearly.domain.ultrahuman;

import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import si.result.eearly.domain.device.DeviceUser;
import si.result.eearly.domain.enums.MeasurementTypeEnum;
import si.result.eearly.domain.measurement.Measurement;
import si.result.eearly.domain.measurement.MeasurementType;

@NoArgsConstructor
@Data
public class UltrahumanMetrics {
    private UltrahumanData data;
    private String error;
    private Integer status;

    @Data
    public static class UltrahumanData {
        @JsonProperty("metric_data")
        private List<UltrahumanMetricData> metricData;

        public UltrahumanData mergeMetrics(UltrahumanData other) {
            Map<String, UltrahumanMetricData> metricDataMap = getMetricData().stream()
                    .collect(Collectors.toMap(UltrahumanMetricData::getType, e -> e, (existing, replacement) -> existing));

            metricData = other.getMetricData().stream()
                    .map(otherData -> {
                        UltrahumanMetricData data = metricDataMap.get(otherData.getType());
                        if (data == null) {
                            return otherData;
                        }

                        data.merge(otherData);
                        return data;
                    })
                    .collect(Collectors.toList());

            return this;
        }
    }

    @Data
    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type", defaultImpl = UltrahumanMetricData.UnknownMetricData.class)
    @JsonSubTypes({
            @JsonSubTypes.Type(value = UltrahumanHeartRate.class, name = "hr"),
            @JsonSubTypes.Type(value = UltrahumanTemperature.class, name = "temp"),
            @JsonSubTypes.Type(value = UltrahumanHrv.class, name = "hrv"),
            @JsonSubTypes.Type(value = UltrahumanSteps.class, name = "steps"),
            @JsonSubTypes.Type(value = UltrahumanNightRhr.class, name = "night_rhr"),
            @JsonSubTypes.Type(value = UltrahumanAvgSleepHrv.class, name = "avg_sleep_hrv"),
            @JsonSubTypes.Type(value = UltrahumanSleep.class, name = "Sleep"),
            @JsonSubTypes.Type(value = UltrahumanSleep.class, name = "sleep"),
            @JsonSubTypes.Type(value = UltrahumanGlucose.class, name = "glucose"),
            @JsonSubTypes.Type(value = UltrahumanMetabolicScore.class, name = "metabolic_score"),
            @JsonSubTypes.Type(value = UltrahumanGlucoseVariability.class, name = "glucose_variability"),
            @JsonSubTypes.Type(value = UltrahumanAverageGlucose.class, name = "average_glucose"),
            @JsonSubTypes.Type(value = UltrahumanHba1c.class, name = "hba1c"),
            @JsonSubTypes.Type(value = UltrahumanTimeInTarget.class, name = "time_in_target"),
            @JsonSubTypes.Type(value = UltrahumanRecoveryIndex.class, name = "recovery_index"),
            @JsonSubTypes.Type(value = UltrahumanMovementIndex.class, name = "movement_index"),
            @JsonSubTypes.Type(value = UltrahumanVo2Max.class, name = "vo2_max"),
            @JsonSubTypes.Type(value = UltrahumanSleepRhr.class, name = "sleep_rhr"),
    })
    public static abstract class UltrahumanMetricData {
        private String type;
        private Object object;

        @Data
        @EqualsAndHashCode(callSuper = true)
        public static class UnknownMetricData extends UltrahumanMetricData {
        }

        public MeasurementTypeEnum getMeasurementType() {
            return MeasurementTypeEnum.UNKNOWN_MEASUREMENT_TYPE;
        }

        public UltrahumanMetricData merge(UltrahumanMetricData other) {
            return this;
        }
    }

    @Data
    public static abstract class UltrahumanObjectBase {
        @JsonProperty("day_start_timestamp")
        private Long dayStartTimestamp;

        public List<Measurement> toMeasurements(DeviceUser deviceUser, MeasurementType measurementType) {
            throw new UnsupportedOperationException("Not implemented");
        }
    }

    @Data
    public static class UltrahumanValue {
        private Double value;
        private Long timestamp;
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class UltrahumanHeartRate extends UltrahumanMetricData {
        private final String type = "hr";
        private UltrahumanHeartRateObject object;

        @Override
        public MeasurementTypeEnum getMeasurementType() {
            return MeasurementTypeEnum.HEART_RATE;
        }

        @Override
        public UltrahumanMetricData merge(UltrahumanMetricData other) {
            if (!(other instanceof UltrahumanHeartRate)) {
                throw new IllegalArgumentException("Cannot merge different types of metrics");
            }
            UltrahumanHeartRate otherHeartRate = (UltrahumanHeartRate) other;

            if (object == null || otherHeartRate.getObject() == null) {
                return this;
            }

            object.getValues().addAll(otherHeartRate.getObject().getValues());

            return this;
        }

        @Data
        @EqualsAndHashCode(callSuper = true)
        @ToString(callSuper = true)
        public static class UltrahumanHeartRateObject extends UltrahumanObjectBase {
            private String title;
            private List<UltrahumanValue> values;
            @JsonProperty("last_reading")
            private Integer lastReading;
            private String unit;

            @Override
            public List<Measurement> toMeasurements(DeviceUser deviceUser, MeasurementType measurementType) {
                List<Measurement> measurements = new ArrayList<>();
                var batchId = UUID.randomUUID();

                for (UltrahumanValue value : values) {
                    Measurement measurement = new Measurement(
                        deviceUser.getProfile().getUserId(),
                        measurementType.getObservationId(),
                        measurementType.getId(),
                        measurementType.getUnit(),
                        value.getValue(),
                        batchId,
                        Instant.ofEpochSecond(value.getTimestamp()).atOffset(ZoneOffset.UTC)
                            .toZonedDateTime(),
                        deviceUser.getDevice().getId()

                    );
                    measurements.add(measurement);
                }

                return measurements;
            }
        }
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class UltrahumanTemperature extends UltrahumanMetricData {
        private final String type = "temp";
        private UltrahumanTemperatureObject object;

        @Data
        @EqualsAndHashCode(callSuper = true)
        @ToString(callSuper = true)
        public static class UltrahumanTemperatureObject extends UltrahumanObjectBase {
            private String title;
            private List<UltrahumanValue> values;
            @JsonProperty("last_reading")
            private Integer lastReading;
            private String unit;
        }
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class UltrahumanHrv extends UltrahumanMetricData {
        private final String type = "hrv";
        private UltrahumanHrvObject object;

        @Override
        public MeasurementTypeEnum getMeasurementType() {
            return MeasurementTypeEnum.HEART_RATE_VARIABILITY;
        }

        @Override
        public UltrahumanMetricData merge(UltrahumanMetricData other) {
            if (!(other instanceof UltrahumanHrv)) {
                throw new IllegalArgumentException("Cannot merge different types of metrics");
            }

            UltrahumanHrv otherHeartRate = (UltrahumanHrv) other;

            if (object == null || otherHeartRate.getObject() == null) {
                return this;
            }

            object.getValues().addAll(otherHeartRate.getObject().getValues());

            return this;
        }

        @Data
        @EqualsAndHashCode(callSuper = true)
        @ToString(callSuper = true)
        public static class UltrahumanHrvObject extends UltrahumanObjectBase {
            private String title;
            private List<UltrahumanValue> values;
            private String subtitle;
            private Double avg;

            @Override
            public List<Measurement> toMeasurements(DeviceUser deviceUser, MeasurementType measurementType) {
                List<Measurement> measurements = new ArrayList<>();
                var batchId = UUID.randomUUID();

                for (UltrahumanValue value : values) {
                  Measurement measurement = new Measurement(
                      deviceUser.getProfile().getUserId(),
                      measurementType.getObservationId(),
                      measurementType.getId(),
                      measurementType.getUnit(),
                      value.getValue(),
                      batchId,
                      Instant.ofEpochSecond(value.getTimestamp()).atOffset(ZoneOffset.UTC)
                          .toZonedDateTime(),
                      deviceUser.getDevice().getId()
                      );

                    measurements.add(measurement);
                }

                return measurements;
            }
        }
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class UltrahumanSteps extends UltrahumanMetricData {
        private final String type = "steps";
        private UltrahumanStepsObject object;

        @Data
        @EqualsAndHashCode(callSuper = true)
        @ToString(callSuper = true)
        public static class UltrahumanStepsObject extends UltrahumanObjectBase {
            private List<UltrahumanValue> values;
            private String subtitle;
            private Double total;
            private Double avg;
        }
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class UltrahumanNightRhr extends UltrahumanMetricData {
        private final String type = "night_rhr";
        private UltrahumanNightRhrObject object;

        @Data
        @EqualsAndHashCode(callSuper = true)
        @ToString(callSuper = true)
        public static class UltrahumanNightRhrObject extends UltrahumanObjectBase {
            private String title;
            private List<UltrahumanValue> values;
            private String subtitle;
            private Double avg;
            @JsonProperty("trend_title")
            private String trendTitle;
            @JsonProperty("trend_direction")
            private String trendDirection;
        }
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class UltrahumanAvgSleepHrv extends UltrahumanMetricData {
        private final String type = "avg_sleep_hrv";
        private UltrahumanAvgSleepHrvObject object;

        @Data
        @EqualsAndHashCode(callSuper = true)
        @ToString(callSuper = true)
        public static class UltrahumanAvgSleepHrvObject extends UltrahumanObjectBase {
            private Integer value;
        }
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class UltrahumanSleep extends UltrahumanMetricData {
        private final String type = "Sleep";
        private UltrahumanSleepObject object;

        @Data
        @EqualsAndHashCode(callSuper = true)
        @ToString(callSuper = true)
        public static class UltrahumanSleepObject extends UltrahumanObjectBase {
            @JsonProperty("bedtime_start")
            private Long bedtimeStart;
            @JsonProperty("bedtime_end")
            private Long bedtimeEnd;
            @JsonProperty("quick_metrics")
            private List<QuickMetric> quickMetrics;
            @JsonProperty("quick_metrics_tiled")
            private List<QuickMetricTiled> quickMetricsTiled;
            @JsonProperty("sleep_stages")
            private List<SleepStage> sleepStages;
            @JsonProperty("sleep_graph")
            private SleepGraph sleepGraph;
            @JsonProperty("movement_graph")
            private MovementGraph movementGraph;
            @JsonProperty("hr_graph")
            private HrGraph hrGraph;
            @JsonProperty("hrv_graph")
            private HrvGraph hrvGraph;
            @JsonProperty("temp_graph")
            private TempGraph tempGraph;
            @JsonProperty("respiratory_graph")
            private RespiratoryGraph respiratoryGraph; // TODO: wasn't tested, null value
            private List<Summary> summary;
            @JsonProperty("sleep_inertia_trend")
            private SleepInertiaTrend sleepInertiaTrend;
            @JsonProperty("sleep_inertia_interpretation")
            private SleepInertiaInterpretation sleepInertiaInterpretation;
            @JsonProperty("score_trend")
            private ScoreTrend scoreTrend;
            @JsonProperty("index_tracking_params")
            private List<TrackingParam> indexTrackingParams;
            private Spo2 spo2;
            @JsonProperty("toss_turn")
            private TossTurn tossTurn;
            @JsonProperty("sleep_cycles")
            private SleepCycles sleepCycles;
        }

        @Data
        public static class QuickMetric {
            private String title;
            @JsonProperty("display_text")
            private String displayText;
            private String unit;
            private Integer value;
            private String deeplink;
            private String type;
            @JsonProperty("education_modal_deeplink")
            private String educationModalDeeplink;
            @JsonProperty("tracking_params")
            private List<TrackingParam> trackingParams;
            @JsonProperty("display_text_marked_up")
            private String displayTextMarkedUp;
        }

        @Data
        public static class TrackingParam {
            @JsonProperty("key_name")
            private String keyName;
            private String value;
        }

        @Data
        public static class QuickMetricTiled {
            private String title;
            private String value;
            private String tag;
            @JsonProperty("tag_color")
            private String tagColor;
            private String deeplink;
            @JsonProperty("trends_unit")
            private String trendsUnit;
            @JsonProperty("trends_value")
            private Integer trendsValue;
            private String type;
        }

        @Data
        public static class SleepStage {
            private String title;
            private String type;
            private Integer percentage;
            @JsonProperty("stage_time_text")
            private String stageTimeText;
            @JsonProperty("stage_time")
            private Integer stageTime;
        }

        @Data
        public static class SleepGraph {
            private String title;
            private List<SleepData> data;
            @JsonProperty("education_modal_deeplink")
            private String educationModalDeeplink;
        }

        @Data
        public static class SleepData {
            private Long start;
            private Long end;
            private String type;
            @JsonProperty("toss_turn")
            private Integer tossTurn;
        }

        @Data
        public static class MovementGraph {
            private String title;
            private List<MovementData> data;
        }

        @Data
        public static class MovementData {
            private Long timestamp;
            private String type;
        }

        @Data
        public static class HrGraph {
            private String title;
            private List<UltrahumanValue> data;
            @JsonProperty("vertical_zone")
            private VerticalZone verticalZone;
            @JsonProperty("info_button")
            private InfoButton infoButton;
            private String unit;
            @JsonProperty("gist_object")
            private GistObject gistObject;
        }

        @Data
        public static class VerticalZone {
            private Long start;
            private Long end;
            private String title;
        }

        @Data
        public static class InfoButton {
            private String title;
            private String deeplink;
        }

        @Data
        public static class GistObject {
            private String title;
            @JsonProperty("detail_text")
            private String detailText;
            @JsonProperty("detail_unit_text")
            private String detailUnitText;
            private String subtitle;
            private Integer avg;
            private Integer min;
            private Integer max;
        }

        @Data
        public static class HrvGraph {
            private String title;
            private List<UltrahumanValue> data;
            private String zone;
            @JsonProperty("info_button")
            private InfoButton infoButton;
            private String unit;
            @JsonProperty("gist_object")
            private GistObject gistObject;
        }

        @Data
        public static class TempGraph {
            private String title;
            private List<UltrahumanValue> data;
            @JsonProperty("info_button")
            private InfoButton infoButton;
            private String unit;
            @JsonProperty("gist_object")
            private GistObject gistObject;
        }

        @Data
        public static class RespiratoryGraph {
        }

        @Data
        public static class Summary {
            private String title;
            private String state;
            @JsonProperty("state_title")
            private String stateTitle;
            private Double score;
            @JsonProperty("education_modal_deeplink")
            private String educationModalDeeplink;
            @JsonProperty("tracking_params")
            private List<TrackingParam> trackingParams;
        }

        @Data
        public static class SleepInertiaTrend {
            private List<UltrahumanValue> data;
            @JsonProperty("gist_object")
            private GistObject gistObject;
            private String title;
            private String unit;
            private Zone zone;
        }

        @Data
        public static class Zone {
            private String title;
            private Integer start;
            private Integer end;
        }

        @Data
        public static class SleepInertiaInterpretation {
            private String header;
            private String title;
            private String subtitle;
            @JsonProperty("education_modal_deeplink")
            private String educationModalDeeplink;
        }

        @Data
        public static class ScoreTrend {
            @JsonProperty("day_avg")
            private Integer dayAvg;
            @JsonProperty("week_avg")
            private Integer weekAvg;
            @JsonProperty("month_avg")
            private Integer monthAvg;
            @JsonProperty("year_avg")
            private Integer yearAvg;
        }

        @Data
        public static class Spo2 {
            private String title;
            private Double value;
            @JsonProperty("is_beta")
            private Boolean isBeta;
            @JsonProperty("education_modal_deeplink")
            private String educationModalDeeplink;
        }

        @Data
        public static class TossTurn {
            private String title;
            private String subtitle;
            private Double value;
            @JsonProperty("is_beta")
            private Boolean isBeta;
            @JsonProperty("education_modal_deeplink")
            private String educationModalDeeplink;
        }

        @Data
        public static class SleepCycles {
            private String title;
            private String infoDeeplink;
            private List<Cycle> cycles;
            private List<Legend> legend;
        }

        @Data
        public static class Cycle {
            private Long startTime;
            private Long endTime;
            private String cycleType;
            private String color;
            @JsonProperty("icon_url")
            private String iconUrl;
        }

        @Data
        public static class Legend {
            private String title;
            private String color;
        }
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class UltrahumanGlucose extends UltrahumanMetricData {
        private final String type = "glucose";
        private UltrahumanGlucoseObject object;

        @Override
        public MeasurementTypeEnum getMeasurementType() {
            return MeasurementTypeEnum.BLOOD_GLUCOSE;
        }

        @Override
        public UltrahumanMetricData merge(UltrahumanMetricData other) {
            if (!(other instanceof UltrahumanGlucose)) {
                throw new IllegalArgumentException("Cannot merge different types of metrics");
            }

            UltrahumanGlucose otherHeartRate = (UltrahumanGlucose) other;

            if (object == null || otherHeartRate.getObject() == null) {
                return this;
            }

            object.getValues().addAll(otherHeartRate.getObject().getValues());

            return this;
        }

        @Data
        @EqualsAndHashCode(callSuper = true)
        @ToString(callSuper = true)
        public static class UltrahumanGlucoseObject extends UltrahumanObjectBase {
            private String title;
            private List<UltrahumanValue> values; // TODO: values property wasn't tested, empty array

            @Override
            public List<Measurement> toMeasurements(DeviceUser deviceUser, MeasurementType measurementType) {
                List<Measurement> measurements = new ArrayList<>();
                var batchId = UUID.randomUUID();

                for (UltrahumanValue value : values) {
                  Measurement measurement = new Measurement(
                      deviceUser.getProfile().getUserId(),
                      measurementType.getObservationId(),
                      measurementType.getId(),
                      measurementType.getUnit(),
                      value.getValue(),
                      batchId,
                      Instant.ofEpochSecond(value.getTimestamp()).atOffset(ZoneOffset.UTC)
                          .toZonedDateTime(),
                      deviceUser.getDevice().getId()
                      );

                    measurements.add(measurement);
                }

                return measurements;
            }
        }
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class UltrahumanMetabolicScore extends UltrahumanMetricData {
        private final String type = "metabolic_score";
        private UltrahumanMetabolicScoreObject object;

        @Data
        @EqualsAndHashCode(callSuper = true)
        @ToString(callSuper = true)
        public static class UltrahumanMetabolicScoreObject extends UltrahumanObjectBase {
            private String title;
            private Integer value; // TODO: value property wasn't tested, null value
        }
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class UltrahumanGlucoseVariability extends UltrahumanMetricData {
        private final String type = "glucose_variability";
        private UltrahumanGlucoseVariabilityObject object;

        @Data
        @EqualsAndHashCode(callSuper = true)
        @ToString(callSuper = true)
        public static class UltrahumanGlucoseVariabilityObject extends UltrahumanObjectBase {
            private String title;
            private Integer value; // TODO: value property wasn't tested, null value
        }
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class UltrahumanAverageGlucose extends UltrahumanMetricData {
        private final String type = "average_glucose";
        private UltrahumanAverageGlucoseObject object;

        @Data
        @EqualsAndHashCode(callSuper = true)
        @ToString(callSuper = true)
        public static class UltrahumanAverageGlucoseObject extends UltrahumanObjectBase {
            private String title;
            private Integer value; // TODO: value property wasn't tested, null value
        }
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class UltrahumanHba1c extends UltrahumanMetricData {
        private final String type = "hba1c";
        private UltrahumanHba1cObject object;

        @Data
        @EqualsAndHashCode(callSuper = true)
        @ToString(callSuper = true)
        public static class UltrahumanHba1cObject extends UltrahumanObjectBase {
            private String title;
            private Integer value; // TODO: value property wasn't tested, null value
        }
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class UltrahumanTimeInTarget extends UltrahumanMetricData {
        private final String type = "time_in_target";
        private UltrahumanTimeInTargetObject object;

        @Data
        @EqualsAndHashCode(callSuper = true)
        @ToString(callSuper = true)
        public static class UltrahumanTimeInTargetObject extends UltrahumanObjectBase {
            private String title;
            private Integer value; // TODO: value property wasn't tested, null value
        }
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class UltrahumanRecoveryIndex extends UltrahumanMetricData {
        private final String type = "recovery_index";
        private UltrahumanRecoveryIndexObject object;

        @Data
        @EqualsAndHashCode(callSuper = true)
        @ToString(callSuper = true)
        public static class UltrahumanRecoveryIndexObject extends UltrahumanObjectBase {
            private String title;
            private Integer value;
        }
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class UltrahumanMovementIndex extends UltrahumanMetricData {
        private final String type = "movement_index";
        private UltrahumanMovementIndexObject object;

        @Data
        @EqualsAndHashCode(callSuper = true)
        @ToString(callSuper = true)
        public static class UltrahumanMovementIndexObject extends UltrahumanObjectBase {
            private String title;
            private Integer value;
        }
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class UltrahumanVo2Max extends UltrahumanMetricData {
        private final String type = "vo2_max";
        private UltrahumanVo2MaxObject object;

        @Data
        @EqualsAndHashCode(callSuper = true)
        @ToString(callSuper = true)
        public static class UltrahumanVo2MaxObject extends UltrahumanObjectBase {
            private String title;
            private Integer value; // TODO: value property wasn't tested, null value
        }
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class UltrahumanSleepRhr extends UltrahumanMetricData {
        private final String type = "sleep_rhr";
        private UltrahumanSleepRhrObject object;

        @Data
        @EqualsAndHashCode(callSuper = true)
        @ToString(callSuper = true)
        public static class UltrahumanSleepRhrObject extends UltrahumanObjectBase {
            private Integer value;
        }
    }
}
