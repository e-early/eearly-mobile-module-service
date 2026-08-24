package si.result.eearly.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public record UltrahumanMetricsDTO(
                UltrahumanDataDTO data,
                String error,
                Integer status) {

        public record UltrahumanDataDTO(
                        @JsonProperty("metric_data") List<UltrahumanMetricDataDTO> metricData) {
        }

        public record UltrahumanMetricDataDTO(
                        String type,
                        UltrahumanObjectBaseDTO object) {
        }

        public interface UltrahumanObjectBaseDTO {
        }

        public record UltrahumanValueDTO(
                        Double value,
                        Long timestamp) {
        }

        public record UltrahumanHeartRateObjectDTO(
                        @JsonProperty("day_start_timestamp") Long dayStartTimestamp,
                        String title,
                        List<UltrahumanValueDTO> values,
                        @JsonProperty("last_reading") Integer lastReading,
                        String unit) implements UltrahumanObjectBaseDTO {
        }

        public record UltrahumanTemperatureObjectDTO(
                        @JsonProperty("day_start_timestamp") Long dayStartTimestamp,
                        String title,
                        List<UltrahumanValueDTO> values,
                        @JsonProperty("last_reading") Integer lastReading,
                        String unit) implements UltrahumanObjectBaseDTO {
        }

        public record UltrahumanHrvObjectDTO(
                        @JsonProperty("day_start_timestamp") Long dayStartTimestamp,
                        String title,
                        List<UltrahumanValueDTO> values,
                        String subtitle,
                        Double avg) implements UltrahumanObjectBaseDTO {
        }

        public record UltrahumanStepsObjectDTO(
                        @JsonProperty("day_start_timestamp") Long dayStartTimestamp,
                        List<UltrahumanValueDTO> values,
                        String subtitle,
                        Double total,
                        Double avg) implements UltrahumanObjectBaseDTO {
        }

        public record UltrahumanNightRhrObjectDTO(
                        @JsonProperty("day_start_timestamp") Long dayStartTimestamp,
                        String title,
                        List<UltrahumanValueDTO> values,
                        String subtitle,
                        Double avg,
                        @JsonProperty("trend_title") String trendTitle,
                        @JsonProperty("trend_direction") String trendDirection) implements UltrahumanObjectBaseDTO {
        }

        public record UltrahumanAvgSleepHrvObjectDTO(
                        @JsonProperty("day_start_timestamp") Long dayStartTimestamp,
                        Integer value) implements UltrahumanObjectBaseDTO {
        }

        public record UltrahumanSleepObjectDTO(
                        @JsonProperty("bedtime_start") Long bedtimeStart,
                        @JsonProperty("bedtime_end") Long bedtimeEnd,
                        @JsonProperty("quick_metrics") List<QuickMetricDTO> quickMetrics,
                        @JsonProperty("quick_metrics_tiled") List<QuickMetricTiledDTO> quickMetricsTiled,
                        @JsonProperty("sleep_stages") List<SleepStageDTO> sleepStages,
                        @JsonProperty("sleep_graph") SleepGraphDTO sleepGraph,
                        @JsonProperty("movement_graph") MovementGraphDTO movementGraph,
                        @JsonProperty("hr_graph") HrGraphDTO hrGraph,
                        @JsonProperty("hrv_graph") HrvGraphDTO hrvGraph,
                        @JsonProperty("temp_graph") TempGraphDTO tempGraph,
                        @JsonProperty("respiratory_graph") RespiratoryGraphDTO respiratoryGraph,
                        List<SummaryDTO> summary,
                        @JsonProperty("sleep_inertia_trend") SleepInertiaTrendDTO sleepInertiaTrend,
                        @JsonProperty("sleep_inertia_interpretation") SleepInertiaInterpretationDTO sleepInertiaInterpretation,
                        @JsonProperty("score_trend") ScoreTrendDTO scoreTrend,
                        @JsonProperty("index_tracking_params") List<TrackingParamDTO> indexTrackingParamscoreTrend,
                        Spo2DTO spo2,
                        @JsonProperty("toss_turn") TossTurnDTO tossTurn,
                        @JsonProperty("sleep_cycles") SleepCyclesDTO sleepCycles) implements UltrahumanObjectBaseDTO {
        }

        public record QuickMetricDTO(
                        String title,
                        @JsonProperty("display_text") String displayText,
                        String unit,
                        Integer value,
                        String deeplink,
                        String type,
                        @JsonProperty("education_modal_deeplink") String educationModalDeeplink,
                        @JsonProperty("tracking_params") List<TrackingParamDTO> trackingParams,
                        @JsonProperty("display_text_marked_up") String displayTextMarkedUp) {
        }

        public record TrackingParamDTO(
                        @JsonProperty("key_name") String keyName,
                        String value) {
        }

        public record QuickMetricTiledDTO(
                        String title,
                        String value,
                        String tag,
                        @JsonProperty("tag_color") String tagColor,
                        String deeplink,
                        @JsonProperty("trends_unit") String trendsUnit,
                        @JsonProperty("trends_value") String trendsValue,
                        String type) {
        }

        public record SleepStageDTO(
                        String title,
                        String type,
                        Integer percentage,
                        @JsonProperty("stage_time_text") String stageTimeText,
                        @JsonProperty("stage_time") String stageTime) {
        }

        public record SleepGraphDTO(
                        String title,
                        List<SleepDataDTO> data,
                        @JsonProperty("education_modal_deeplink") String educationModalDeeplink) {
        }

        public record SleepDataDTO(
                        Long start,
                        Long end,
                        String type,
                        @JsonProperty("toss_turn") Integer tossTurn) {
        }

        public record MovementGraphDTO(
                        String title,
                        List<MovementDataDTO> data) {
        }

        public record MovementDataDTO(
                        Long timestamp,
                        String type) {
        }

        public record HrGraphDTO(
                        String title,
                        List<UltrahumanValueDTO> data,
                        @JsonProperty("vertical_zone") VerticalZoneDTO verticalZone,
                        @JsonProperty("info_button") InfoButtonDTO infoButton,
                        String unit,
                        @JsonProperty("gist_object") GistObjectDTO gistObject) {
        }

        public record VerticalZoneDTO(
                        Long start,
                        Long end,
                        String title) {
        }

        public record InfoButtonDTO(
                        String title,
                        String deeplink) {
        }

        public record GistObjectDTO(
                        String title,
                        @JsonProperty("detail_text") String detailText,
                        @JsonProperty("detail_unit_text") String detailUnitText,
                        String subtitle,
                        Integer avg,
                        Integer min,
                        Integer max) {
        }

        public record HrvGraphDTO(
                        String title,
                        List<UltrahumanValueDTO> data,
                        String zone,
                        @JsonProperty("info_button") InfoButtonDTO infoButton,
                        String unit,
                        @JsonProperty("gist_object") GistObjectDTO gistObject) {
        }

        public record TempGraphDTO(
                        String title,
                        List<UltrahumanValueDTO> data,
                        @JsonProperty("info_button") InfoButtonDTO infoButton,
                        String unit,
                        @JsonProperty("gist_object") GistObjectDTO gistObject) {
        }

        public record RespiratoryGraphDTO() {
        }

        public record SummaryDTO(
                        String title,
                        String state,
                        @JsonProperty("state_title") String stateTitle,
                        Double score,
                        @JsonProperty("education_modal_deeplink") String educationModalDeeplink,
                        @JsonProperty("tracking_params") List<TrackingParamDTO> trackingParams) {
        }

        public record SleepInertiaTrendDTO(
                        List<UltrahumanValueDTO> data,
                        @JsonProperty("gist_object") GistObjectDTO gistObject,
                        String title,
                        String unit,
                        ZoneDTO zone) {
        }

        public record ZoneDTO(
                        String title,
                        Integer start,
                        Integer end) {
        }

        public record SleepInertiaInterpretationDTO(
                        String header,
                        String title,
                        String subtitle,
                        @JsonProperty("education_modal_deeplink") String educationModalDeeplink) {
        }

        public record ScoreTrendDTO(
                        @JsonProperty("day_avg") Integer dayAvg,
                        @JsonProperty("week_avg") Integer weekAvg,
                        @JsonProperty("month_avg") Integer monthAvg,
                        @JsonProperty("year_avg") Integer yearAvg) {
        }

        public record Spo2DTO(
                        String title,
                        Double value,
                        @JsonProperty("is_beta") Boolean isBeta,
                        @JsonProperty("education_modal_deeplink") String educationModalDeeplink) {
        }

        public record TossTurnDTO(
                        String title,
                        String subtitle,
                        Double value,
                        @JsonProperty("is_beta") Boolean isBeta,
                        @JsonProperty("education_modal_deeplink") String educationModalDeeplink) {
        }

        public record SleepCyclesDTO(
                        String title,
                        String infoDeeplink,
                        List<CycleDTO> cycles,
                        List<LegendDTO> legend) {
        }

        public record CycleDTO(
                        Long startTime,
                        Long endTime,
                        String cycleType,
                        String color,
                        @JsonProperty("icon_url") String iconUrl) {
        }

        public record LegendDTO(
                        String title,
                        String color) {
        }

        public record UltrahumanGlucoseObjectDTO(
                        @JsonProperty("day_start_timestamp") Long dayStartTimestamp,
                        String title,
                        List<UltrahumanValueDTO> values) implements UltrahumanObjectBaseDTO {
        }

        public record UltrahumanMetabolicScoreObjectDTO(
                        @JsonProperty("day_start_timestamp") Long dayStartTimestamp,
                        String title,
                        Integer value) implements UltrahumanObjectBaseDTO {
        }

        public record UltrahumanGlucoseVariabilityObjectDTO(
                        @JsonProperty("day_start_timestamp") Long dayStartTimestamp,
                        String title,
                        Integer value) implements UltrahumanObjectBaseDTO {
        }

        public record UltrahumanAverageGlucoseObjectDTO(
                        @JsonProperty("day_start_timestamp") Long dayStartTimestamp,
                        String title,
                        Integer value) implements UltrahumanObjectBaseDTO {
        }

        public record UltrahumanHba1cObjectDTO(
                        @JsonProperty("day_start_timestamp") Long dayStartTimestamp,
                        String title,
                        Integer value) implements UltrahumanObjectBaseDTO {
        }

        public record UltrahumanTimeInTargetObjectDTO(
                        @JsonProperty("day_start_timestamp") Long dayStartTimestamp,
                        String title,
                        Integer value) implements UltrahumanObjectBaseDTO {
        }

        public record UltrahumanRecoveryIndexObjectDTO(
                        @JsonProperty("day_start_timestamp") Long dayStartTimestamp,
                        String title,
                        Integer value) implements UltrahumanObjectBaseDTO {
        }

        public record UltrahumanMovementIndexObjectDTO(
                        @JsonProperty("day_start_timestamp") Long dayStartTimestamp,
                        String title,
                        Integer value) implements UltrahumanObjectBaseDTO {
        }

        public record UltrahumanVo2MaxObjectDTO(
                        @JsonProperty("day_start_timestamp") Long dayStartTimestamp,
                        String title,
                        Integer value) implements UltrahumanObjectBaseDTO {
        }

        public record UltrahumanSleepRhrObjectDTO(
                        @JsonProperty("day_start_timestamp") Long dayStartTimestamp,
                        Integer value) implements UltrahumanObjectBaseDTO {
        }
}
