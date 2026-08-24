package si.result.eearly.mapper;

import java.util.List;
import java.util.stream.Collectors;

import org.mapstruct.Mapper;

import si.result.eearly.domain.ultrahuman.UltrahumanMetrics;
import si.result.eearly.domain.ultrahuman.UltrahumanMetrics.UltrahumanAverageGlucose.UltrahumanAverageGlucoseObject;
import si.result.eearly.domain.ultrahuman.UltrahumanMetrics.UltrahumanAvgSleepHrv.UltrahumanAvgSleepHrvObject;
import si.result.eearly.domain.ultrahuman.UltrahumanMetrics.UltrahumanGlucose.UltrahumanGlucoseObject;
import si.result.eearly.domain.ultrahuman.UltrahumanMetrics.UltrahumanGlucoseVariability.UltrahumanGlucoseVariabilityObject;
import si.result.eearly.domain.ultrahuman.UltrahumanMetrics.UltrahumanHba1c.UltrahumanHba1cObject;
import si.result.eearly.domain.ultrahuman.UltrahumanMetrics.UltrahumanHeartRate.UltrahumanHeartRateObject;
import si.result.eearly.domain.ultrahuman.UltrahumanMetrics.UltrahumanHrv.UltrahumanHrvObject;
import si.result.eearly.domain.ultrahuman.UltrahumanMetrics.UltrahumanMetabolicScore.UltrahumanMetabolicScoreObject;
import si.result.eearly.domain.ultrahuman.UltrahumanMetrics.UltrahumanMovementIndex.UltrahumanMovementIndexObject;
import si.result.eearly.domain.ultrahuman.UltrahumanMetrics.UltrahumanNightRhr.UltrahumanNightRhrObject;
import si.result.eearly.domain.ultrahuman.UltrahumanMetrics.UltrahumanRecoveryIndex.UltrahumanRecoveryIndexObject;
import si.result.eearly.domain.ultrahuman.UltrahumanMetrics.UltrahumanSleep.UltrahumanSleepObject;
import si.result.eearly.domain.ultrahuman.UltrahumanMetrics.UltrahumanSleepRhr.UltrahumanSleepRhrObject;
import si.result.eearly.domain.ultrahuman.UltrahumanMetrics.UltrahumanSteps.UltrahumanStepsObject;
import si.result.eearly.domain.ultrahuman.UltrahumanMetrics.UltrahumanTemperature.UltrahumanTemperatureObject;
import si.result.eearly.domain.ultrahuman.UltrahumanMetrics.UltrahumanTimeInTarget.UltrahumanTimeInTargetObject;
import si.result.eearly.domain.ultrahuman.UltrahumanMetrics.UltrahumanVo2Max.UltrahumanVo2MaxObject;
import si.result.eearly.dto.UltrahumanMetricsDTO;
import si.result.eearly.dto.UltrahumanMetricsDTO.UltrahumanAverageGlucoseObjectDTO;
import si.result.eearly.dto.UltrahumanMetricsDTO.UltrahumanAvgSleepHrvObjectDTO;
import si.result.eearly.dto.UltrahumanMetricsDTO.UltrahumanGlucoseObjectDTO;
import si.result.eearly.dto.UltrahumanMetricsDTO.UltrahumanGlucoseVariabilityObjectDTO;
import si.result.eearly.dto.UltrahumanMetricsDTO.UltrahumanHba1cObjectDTO;
import si.result.eearly.dto.UltrahumanMetricsDTO.UltrahumanHeartRateObjectDTO;
import si.result.eearly.dto.UltrahumanMetricsDTO.UltrahumanHrvObjectDTO;
import si.result.eearly.dto.UltrahumanMetricsDTO.UltrahumanMetabolicScoreObjectDTO;
import si.result.eearly.dto.UltrahumanMetricsDTO.UltrahumanMovementIndexObjectDTO;
import si.result.eearly.dto.UltrahumanMetricsDTO.UltrahumanNightRhrObjectDTO;
import si.result.eearly.dto.UltrahumanMetricsDTO.UltrahumanRecoveryIndexObjectDTO;
import si.result.eearly.dto.UltrahumanMetricsDTO.UltrahumanSleepObjectDTO;
import si.result.eearly.dto.UltrahumanMetricsDTO.UltrahumanSleepRhrObjectDTO;
import si.result.eearly.dto.UltrahumanMetricsDTO.UltrahumanStepsObjectDTO;
import si.result.eearly.dto.UltrahumanMetricsDTO.UltrahumanTemperatureObjectDTO;
import si.result.eearly.dto.UltrahumanMetricsDTO.UltrahumanTimeInTargetObjectDTO;
import si.result.eearly.dto.UltrahumanMetricsDTO.UltrahumanVo2MaxObjectDTO;
import si.result.eearly.genproto.Cycle;
import si.result.eearly.genproto.GetMetricsResponse;
import si.result.eearly.genproto.GistObject;
import si.result.eearly.genproto.HrGraph;
import si.result.eearly.genproto.HrvGraph;
import si.result.eearly.genproto.InfoButton;
import si.result.eearly.genproto.Legend;
import si.result.eearly.genproto.MovementData;
import si.result.eearly.genproto.MovementGraph;
import si.result.eearly.genproto.QuickMetric;
import si.result.eearly.genproto.QuickMetricTiled;
import si.result.eearly.genproto.ScoreTrend;
import si.result.eearly.genproto.SleepCycles;
import si.result.eearly.genproto.SleepData;
import si.result.eearly.genproto.SleepGraph;
import si.result.eearly.genproto.SleepInertiaInterpretation;
import si.result.eearly.genproto.SleepInertiaTrend;
import si.result.eearly.genproto.SleepStage;
import si.result.eearly.genproto.Spo2;
import si.result.eearly.genproto.Summary;
import si.result.eearly.genproto.TempGraph;
import si.result.eearly.genproto.TossTurn;
import si.result.eearly.genproto.TrackingParam;
import si.result.eearly.genproto.UltrahumanData;
import si.result.eearly.genproto.UltrahumanMetricData;
import si.result.eearly.genproto.UltrahumanObject;
import si.result.eearly.genproto.UltrahumanValue;
import si.result.eearly.genproto.VerticalZone;
import si.result.eearly.genproto.Zone;

@Mapper
public interface UltrahumanMapper {

	UltrahumanMetricsDTO toMetricsDTO(UltrahumanMetrics metrics);

	default UltrahumanMetricsDTO.UltrahumanObjectBaseDTO map(Object value) {
		if (!(value instanceof UltrahumanMetrics.UltrahumanObjectBase)) {
			return null;
		}
		if (value instanceof UltrahumanHeartRateObject) {
			return toDTO((UltrahumanHeartRateObject) value);
		}
		if (value instanceof UltrahumanTemperatureObject) {
			return toDTO((UltrahumanTemperatureObject) value);
		}
		if (value instanceof UltrahumanHrvObject) {
			return toDTO((UltrahumanHrvObject) value);
		}
		if (value instanceof UltrahumanStepsObject) {
			return toDTO((UltrahumanStepsObject) value);
		}
		if (value instanceof UltrahumanNightRhrObject) {
			return toDTO((UltrahumanNightRhrObject) value);
		}
		if (value instanceof UltrahumanAvgSleepHrvObject) {
			return toDTO((UltrahumanAvgSleepHrvObject) value);
		}
		if (value instanceof UltrahumanSleepObject) {
			return toDTO((UltrahumanSleepObject) value);
		}
		if (value instanceof UltrahumanGlucoseObject) {
			return toDTO((UltrahumanGlucoseObject) value);
		}
		if (value instanceof UltrahumanMetabolicScoreObject) {
			return toDTO((UltrahumanMetabolicScoreObject) value);
		}
		if (value instanceof UltrahumanGlucoseVariabilityObject) {
			return toDTO((UltrahumanGlucoseVariabilityObject) value);
		}
		if (value instanceof UltrahumanAverageGlucoseObject) {
			return toDTO((UltrahumanAverageGlucoseObject) value);
		}
		if (value instanceof UltrahumanHba1cObject) {
			return toDTO((UltrahumanHba1cObject) value);
		}
		if (value instanceof UltrahumanTimeInTargetObject) {
			return toDTO((UltrahumanTimeInTargetObject) value);
		}
		if (value instanceof UltrahumanRecoveryIndexObject) {
			return toDTO((UltrahumanRecoveryIndexObject) value);
		}
		if (value instanceof UltrahumanMovementIndexObject) {
			return toDTO((UltrahumanMovementIndexObject) value);
		}
		if (value instanceof UltrahumanVo2MaxObject) {
			return toDTO((UltrahumanVo2MaxObject) value);
		}
		if (value instanceof UltrahumanSleepRhrObject) {
			return toDTO((UltrahumanSleepRhrObject) value);
		}

		return null;
	}

	UltrahumanHeartRateObjectDTO toDTO(UltrahumanHeartRateObject value);

	UltrahumanTemperatureObjectDTO toDTO(UltrahumanTemperatureObject value);

	UltrahumanHrvObjectDTO toDTO(UltrahumanHrvObject value);

	UltrahumanStepsObjectDTO toDTO(UltrahumanStepsObject value);

	UltrahumanNightRhrObjectDTO toDTO(UltrahumanNightRhrObject value);

	UltrahumanAvgSleepHrvObjectDTO toDTO(UltrahumanAvgSleepHrvObject value);

	UltrahumanSleepObjectDTO toDTO(UltrahumanSleepObject value);

	UltrahumanGlucoseObjectDTO toDTO(UltrahumanGlucoseObject value);

	UltrahumanMetabolicScoreObjectDTO toDTO(UltrahumanMetabolicScoreObject value);

	UltrahumanGlucoseVariabilityObjectDTO toDTO(UltrahumanGlucoseVariabilityObject value);

	UltrahumanAverageGlucoseObjectDTO toDTO(UltrahumanAverageGlucoseObject value);

	UltrahumanHba1cObjectDTO toDTO(UltrahumanHba1cObject value);

	UltrahumanTimeInTargetObjectDTO toDTO(UltrahumanTimeInTargetObject value);

	UltrahumanRecoveryIndexObjectDTO toDTO(UltrahumanRecoveryIndexObject value);

	UltrahumanMovementIndexObjectDTO toDTO(UltrahumanMovementIndexObject value);

	UltrahumanVo2MaxObjectDTO toDTO(UltrahumanVo2MaxObject value);

	UltrahumanSleepRhrObjectDTO toDTO(UltrahumanSleepRhrObject value);

	// TODO: remove when RespiratoryGraph will have fields
	UltrahumanMetricsDTO.RespiratoryGraphDTO map(UltrahumanMetrics.UltrahumanSleep.RespiratoryGraph value);

	default List<UltrahumanValue> toUltrahumanValuesGrpc(List<UltrahumanMetrics.UltrahumanValue> values) {
		return values
				.stream()
				.map(value -> {
					UltrahumanValue.Builder builder = UltrahumanValue
							.newBuilder()
							.setTimestamp(value.getTimestamp());

					if (value.getValue() != null) {
						builder.setValue(value.getValue());
					}

					return builder.build();
				})
				.collect(Collectors.toList());
	}

	default List<TrackingParam> toTrackingParamsGrpc(List<UltrahumanMetrics.UltrahumanSleep.TrackingParam> values) {
		return values
				.stream()
				.map(value -> TrackingParam
						.newBuilder()
						.setKeyName(value.getKeyName())
						.setValue(value.getValue())
						.build())
				.collect(Collectors.toList());
	}

	default HrvGraph toHrvGraphGrpc(UltrahumanMetrics.UltrahumanSleep.HrvGraph value) {
		HrvGraph.Builder builder = HrvGraph.newBuilder()
				.setTitle(value.getTitle())
				.addAllData(toUltrahumanValuesGrpc(value.getData()))
				.setInfoButton(toInfoButtonGrpc(value.getInfoButton()))
				.setUnit(value.getUnit())
				.setGistObject(toGistObjectGrpc(value.getGistObject()));

		if (value.getZone() != null) {
			builder.setZone(value.getZone());
		}

		return builder.build();
	}

	default TempGraph toTempGraphGrpc(UltrahumanMetrics.UltrahumanSleep.TempGraph value) {
		TempGraph.Builder builder = TempGraph.newBuilder()
				.setTitle(value.getTitle())
				.addAllData(toUltrahumanValuesGrpc(value.getData()))
				.setInfoButton(toInfoButtonGrpc(value.getInfoButton()))
				.setGistObject(toGistObjectGrpc(value.getGistObject()));

		if (value.getUnit() != null) {
			builder.setUnit(value.getUnit());
		}

		return builder.build();
	}

	default InfoButton toInfoButtonGrpc(UltrahumanMetrics.UltrahumanSleep.InfoButton value) {
		return InfoButton.newBuilder()
				.setTitle(value.getTitle())
				.setDeeplink(value.getDeeplink())
				.build();
	}

	default GistObject toGistObjectGrpc(UltrahumanMetrics.UltrahumanSleep.GistObject value) {
		GistObject.Builder builder = GistObject.newBuilder()
				.setTitle(value.getTitle())
				.setAvg(value.getAvg())
				.setMin(value.getMin())
				.setMax(value.getMax());

		if (value.getSubtitle() != null) {
			builder.setSubtitle(value.getSubtitle());
		}
		if (value.getDetailText() != null) {
			builder.setDetailText(value.getDetailText());
		}
		if (value.getDetailUnitText() != null) {
			builder.setDetailUnitText(value.getDetailText());
		}

		return builder.build();
	}

	default SleepInertiaInterpretation toSleepInertiaInterpretationGrpc(
			UltrahumanMetrics.UltrahumanSleep.SleepInertiaInterpretation value) {
		SleepInertiaInterpretation.Builder builder = SleepInertiaInterpretation.newBuilder()
				.setHeader(value.getHeader())
				.setTitle(value.getTitle())
				.setSubtitle(value.getSubtitle());

		if (value.getEducationModalDeeplink() != null) {
			builder.setEducationModalDeeplink(value.getEducationModalDeeplink());
		}

		return builder.build();
	}

	default GetMetricsResponse toMetricsGrpc(UltrahumanMetrics metrics) {
		UltrahumanData.Builder dataBuilder = UltrahumanData.newBuilder();

		for (UltrahumanMetrics.UltrahumanMetricData metricData : metrics.getData().getMetricData()) {

			UltrahumanObject.Builder objectBuilder = UltrahumanObject.newBuilder();

			if (metricData.getObject() instanceof UltrahumanHeartRateObject) {
				UltrahumanHeartRateObject domainObject = (UltrahumanHeartRateObject) metricData.getObject();

				objectBuilder
						.setDayStartTimestamp(domainObject.getDayStartTimestamp())
						.setTitle(domainObject.getTitle())
						.addAllValues(toUltrahumanValuesGrpc(domainObject.getValues()))
						.setLastReading(domainObject.getLastReading())
						.setUnit(domainObject.getUnit());
			}
			if (metricData.getObject() instanceof UltrahumanTemperatureObject) {
				UltrahumanTemperatureObject domainObject = (UltrahumanTemperatureObject) metricData.getObject();

				objectBuilder
						.setDayStartTimestamp(domainObject.getDayStartTimestamp())
						.setTitle(domainObject.getTitle())
						.addAllValues(toUltrahumanValuesGrpc(domainObject.getValues()))
						.setLastReading(domainObject.getLastReading())
						.setUnit(domainObject.getUnit());
			}
			if (metricData.getObject() instanceof UltrahumanHrvObject) {
				UltrahumanHrvObject domainObject = (UltrahumanHrvObject) metricData.getObject();

				objectBuilder
						.setDayStartTimestamp(domainObject.getDayStartTimestamp())
						.setTitle(domainObject.getTitle())
						.addAllValues(toUltrahumanValuesGrpc(domainObject.getValues()))
						.setSubtitle(domainObject.getSubtitle());

				if (domainObject.getAvg() != null) {
					objectBuilder.setAvg(domainObject.getAvg());
				}
			}
			if (metricData.getObject() instanceof UltrahumanStepsObject) {
				UltrahumanStepsObject domainObject = (UltrahumanStepsObject) metricData.getObject();

				objectBuilder
						.setDayStartTimestamp(domainObject.getDayStartTimestamp())
						.addAllValues(toUltrahumanValuesGrpc(domainObject.getValues()))
						.setSubtitle(domainObject.getSubtitle());

				if (domainObject.getTotal() != null) {
					objectBuilder.setTotal(domainObject.getTotal());
				}
				if (domainObject.getAvg() != null) {
					objectBuilder.setAvg(domainObject.getAvg());
				}
			}
			if (metricData.getObject() instanceof UltrahumanNightRhrObject) {
				UltrahumanNightRhrObject domainObject = (UltrahumanNightRhrObject) metricData.getObject();

				objectBuilder
						.setDayStartTimestamp(domainObject.getDayStartTimestamp())
						.setTitle(domainObject.getTitle())
						.addAllValues(toUltrahumanValuesGrpc(domainObject.getValues()))
						.setSubtitle(domainObject.getSubtitle())
						.setAvg(domainObject.getAvg())
						.setTrendTitle(domainObject.getTrendTitle())
						.setTrendDirection(domainObject.getTrendDirection());
			}
			if (metricData.getObject() instanceof UltrahumanAvgSleepHrvObject) {
				UltrahumanAvgSleepHrvObject domainObject = (UltrahumanAvgSleepHrvObject) metricData.getObject();

				objectBuilder
						.setDayStartTimestamp(domainObject.getDayStartTimestamp())
						.setValue(domainObject.getValue());
			}
			if (metricData.getObject() instanceof UltrahumanSleepObject) {
				UltrahumanSleepObject domainObject = (UltrahumanSleepObject) metricData.getObject();

				objectBuilder
						.setBedtimeStart(domainObject.getBedtimeStart())
						.setBedtimeEnd(domainObject.getBedtimeEnd())
						.addAllQuickMetrics(domainObject.getQuickMetrics().stream()
								.map(value -> {
									QuickMetric.Builder builder = QuickMetric
											.newBuilder()
											.setTitle(value.getTitle())
											.setDisplayText(value.getDisplayText())
											.setValue(value.getValue())
											.setType(value.getType())
											.addAllTrackingParams(toTrackingParamsGrpc(value.getTrackingParams()));

									if (value.getUnit() != null) {
										builder.setUnit(value.getUnit());
									}
									if (value.getDeeplink() != null) {
										builder.setDeeplink(value.getDeeplink());
									}
									if (value.getEducationModalDeeplink() != null) {
										builder.setEducationModalDeeplink(value.getEducationModalDeeplink());
									}
									if (value.getDisplayTextMarkedUp() != null) {
										builder.setDisplayTextMarkedUp(value.getDisplayTextMarkedUp());
									}
									return builder.build();
								})
								.collect(Collectors.toList()))
						.addAllQuickMetricsTiled(domainObject.getQuickMetricsTiled().stream()
								.map(value -> {
									QuickMetricTiled.Builder builder = QuickMetricTiled
											.newBuilder()
											.setTitle(value.getTitle())
											.setValue(value.getValue())
											.setTag(value.getTag())
											.setTagColor(value.getTagColor())
											.setDeeplink(value.getDeeplink())
											.setTrendsUnit(value.getTrendsUnit())
											.setType(value.getType());

									if (value.getTrendsValue() != null) {
										builder.setTrendsValue(value.getTrendsValue());
									}
									return builder.build();
								})
								.collect(Collectors.toList()))
						.addAllSleepStages(domainObject.getSleepStages().stream()
								.map(value -> SleepStage
										.newBuilder()
										.setTitle(value.getTitle())
										.setType(value.getType())
										.setPercentage(value.getPercentage())
										.setStageTimeText(value.getStageTimeText())
										.setStageTime(value.getStageTime())
										.build())
								.collect(Collectors.toList()))
						.setSleepGraph(SleepGraph.newBuilder()
								.setTitle(domainObject.getSleepGraph().getTitle())
								.addAllData(domainObject.getSleepGraph().getData().stream()
										.map(value -> {
											SleepData.Builder builder = SleepData.newBuilder()
													.setStart(value.getStart())
													.setEnd(value.getEnd())
													.setType(value.getType());

											if (value.getTossTurn() != null) {
												builder.setTossTurn(value.getTossTurn());
											}
											return builder.build();
										})
										.collect(Collectors.toList()))
								.setEducationModalDeeplink(domainObject.getSleepGraph().getEducationModalDeeplink())
								.build())
						.setMovementGraph(MovementGraph.newBuilder()
								.setTitle(domainObject.getMovementGraph().getTitle())
								.addAllData(domainObject.getMovementGraph().getData().stream()
										.map(value -> MovementData
												.newBuilder()
												.setTimestamp(value.getTimestamp())
												.setType(value.getType())
												.build())
										.collect(Collectors.toList()))
								.build())
						.setHrGraph(HrGraph.newBuilder()
								.setTitle(domainObject.getHrGraph().getTitle())
								.addAllData(toUltrahumanValuesGrpc(domainObject.getHrGraph().getData()))
								.setVerticalZone(
										VerticalZone.newBuilder()
												.setStart(domainObject.getHrGraph().getVerticalZone().getStart())
												.setEnd(domainObject.getHrGraph().getVerticalZone().getEnd())
												.setTitle(domainObject.getHrGraph().getVerticalZone().getTitle())
												.build())
								.setInfoButton(toInfoButtonGrpc(domainObject.getHrGraph().getInfoButton()))
								.setUnit(domainObject.getHrGraph().getUnit())
								.setGistObject(toGistObjectGrpc(domainObject.getHrGraph().getGistObject()))
								.build())
						.setHrvGraph(toHrvGraphGrpc(domainObject.getHrvGraph()))
						.setTempGraph(toTempGraphGrpc(domainObject.getTempGraph()))
						// TODO: setRespiratoryGraph
						.addAllSummary(domainObject.getSummary().stream()
								.map(value -> Summary
										.newBuilder()
										.setTitle(value.getTitle())
										.setState(value.getState())
										.setStateTitle(value.getStateTitle())
										.setScore(value.getScore())
										.setEducationModalDeeplink(value.getEducationModalDeeplink())
										.addAllTrackingParams(toTrackingParamsGrpc(value.getTrackingParams()))
										.build())
								.collect(Collectors.toList()))
						.setSleepInertiaTrend(SleepInertiaTrend.newBuilder()
								.addAllData(toUltrahumanValuesGrpc(domainObject.getSleepInertiaTrend().getData()))
								.setGistObject(toGistObjectGrpc(domainObject.getSleepInertiaTrend().getGistObject()))
								.setTitle(domainObject.getSleepInertiaTrend().getTitle())
								.setUnit(domainObject.getSleepInertiaTrend().getUnit())
								.setZone(Zone.newBuilder()
										.setTitle(domainObject.getSleepInertiaTrend().getZone().getTitle())
										.setStart(domainObject.getSleepInertiaTrend().getZone().getStart())
										.setEnd(domainObject.getSleepInertiaTrend().getZone().getEnd())
										.build())
								.build())
						.setSleepInertiaInterpretation(
								toSleepInertiaInterpretationGrpc(domainObject.getSleepInertiaInterpretation()))
						.setScoreTrend(ScoreTrend.newBuilder()
								.setDayAvg(domainObject.getScoreTrend().getDayAvg())
								.setWeekAvg(domainObject.getScoreTrend().getWeekAvg())
								.setMonthAvg(domainObject.getScoreTrend().getMonthAvg())
								.setYearAvg(domainObject.getScoreTrend().getYearAvg())
								.build())
						.addAllIndexTrackingParamscoreTrend(toTrackingParamsGrpc(domainObject.getIndexTrackingParams()))
						.setSpo2(Spo2.newBuilder()
								.setTitle(domainObject.getSpo2().getTitle())
								.setValue(domainObject.getSpo2().getValue())
								.setIsBeta(domainObject.getSpo2().getIsBeta())
								.setEducationModalDeeplink(domainObject.getSpo2().getEducationModalDeeplink())
								.build())
						.setTossTurn(TossTurn.newBuilder()
								.setTitle(domainObject.getTossTurn().getTitle())
								.setSubtitle(domainObject.getTossTurn().getSubtitle())
								.setValue(domainObject.getTossTurn().getValue())
								.setIsBeta(domainObject.getTossTurn().getIsBeta())
								.setEducationModalDeeplink(domainObject.getTossTurn().getEducationModalDeeplink())
								.build())
						.setSleepCycles(SleepCycles.newBuilder()
								.setTitle(domainObject.getSleepCycles().getTitle())
								.setInfoDeeplink(domainObject.getSleepCycles().getInfoDeeplink())
								.addAllCycles(domainObject.getSleepCycles().getCycles().stream()
										.map(value -> {
											Cycle.Builder builder = Cycle.newBuilder()
													.setStartTime(value.getStartTime())
													.setEndTime(value.getEndTime())
													.setCycleType(value.getCycleType());

											if (value.getColor() != null) {
												builder.setColor(value.getColor());
											}
											if (value.getIconUrl() != null) {
												builder.setIconUrl(value.getIconUrl());
											}
											return builder.build();
										})
										.collect(Collectors.toList()))
								.addAllLegend(domainObject.getSleepCycles().getLegend().stream()
										.map(value -> {
											Legend.Builder builder = Legend.newBuilder()
													.setTitle(value.getTitle())
													.setColor(value.getColor());

											return builder.build();
										})
										.collect(Collectors.toList()))
								.build());
			}
			if (metricData.getObject() instanceof UltrahumanGlucoseObject) {
				UltrahumanGlucoseObject domainObject = (UltrahumanGlucoseObject) metricData.getObject();

				objectBuilder
						.setDayStartTimestamp(domainObject.getDayStartTimestamp())
						.setTitle(domainObject.getTitle())
						.addAllValues(toUltrahumanValuesGrpc(domainObject.getValues()));
			}
			if (metricData.getObject() instanceof UltrahumanMetabolicScoreObject) {
				UltrahumanMetabolicScoreObject domainObject = (UltrahumanMetabolicScoreObject) metricData.getObject();

				objectBuilder
						.setDayStartTimestamp(domainObject.getDayStartTimestamp())
						.setTitle(domainObject.getTitle());

				if (domainObject.getValue() != null) {
					objectBuilder.setValue(domainObject.getValue());
				}
			}
			if (metricData.getObject() instanceof UltrahumanGlucoseVariabilityObject) {
				UltrahumanGlucoseVariabilityObject domainObject = (UltrahumanGlucoseVariabilityObject) metricData.getObject();

				objectBuilder
						.setDayStartTimestamp(domainObject.getDayStartTimestamp())
						.setTitle(domainObject.getTitle());

				if (domainObject.getValue() != null) {
					objectBuilder.setValue(domainObject.getValue());
				}
			}
			if (metricData.getObject() instanceof UltrahumanAverageGlucoseObject) {
				UltrahumanAverageGlucoseObject domainObject = (UltrahumanAverageGlucoseObject) metricData.getObject();

				objectBuilder
						.setDayStartTimestamp(domainObject.getDayStartTimestamp())
						.setTitle(domainObject.getTitle());

				if (domainObject.getValue() != null) {
					objectBuilder.setValue(domainObject.getValue());
				}
			}
			if (metricData.getObject() instanceof UltrahumanHba1cObject) {
				UltrahumanHba1cObject domainObject = (UltrahumanHba1cObject) metricData.getObject();

				objectBuilder
						.setDayStartTimestamp(domainObject.getDayStartTimestamp())
						.setTitle(domainObject.getTitle());

				if (domainObject.getValue() != null) {
					objectBuilder.setValue(domainObject.getValue());
				}
			}
			if (metricData.getObject() instanceof UltrahumanTimeInTargetObject) {
				UltrahumanTimeInTargetObject domainObject = (UltrahumanTimeInTargetObject) metricData.getObject();

				objectBuilder
						.setDayStartTimestamp(domainObject.getDayStartTimestamp())
						.setTitle(domainObject.getTitle());

				if (domainObject.getValue() != null) {
					objectBuilder.setValue(domainObject.getValue());
				}
			}
			if (metricData.getObject() instanceof UltrahumanRecoveryIndexObject) {
				UltrahumanRecoveryIndexObject domainObject = (UltrahumanRecoveryIndexObject) metricData.getObject();

				objectBuilder
						.setDayStartTimestamp(domainObject.getDayStartTimestamp())
						.setTitle(domainObject.getTitle())
						.setValue(domainObject.getValue());
			}
			if (metricData.getObject() instanceof UltrahumanMovementIndexObject) {
				UltrahumanMovementIndexObject domainObject = (UltrahumanMovementIndexObject) metricData.getObject();

				objectBuilder
						.setDayStartTimestamp(domainObject.getDayStartTimestamp())
						.setTitle(domainObject.getTitle())
						.setValue(domainObject.getValue());
			}
			if (metricData.getObject() instanceof UltrahumanVo2MaxObject) {
				UltrahumanVo2MaxObject domainObject = (UltrahumanVo2MaxObject) metricData.getObject();

				objectBuilder
						.setDayStartTimestamp(domainObject.getDayStartTimestamp())
						.setTitle(domainObject.getTitle());

				if (domainObject.getValue() != null) {
					objectBuilder.setValue(domainObject.getValue());
				}
			}
			if (metricData.getObject() instanceof UltrahumanSleepRhrObject) {
				UltrahumanSleepRhrObject domainObject = (UltrahumanSleepRhrObject) metricData.getObject();

				objectBuilder
						.setDayStartTimestamp(domainObject.getDayStartTimestamp());

				if (domainObject.getValue() != null) {
					objectBuilder.setValue(domainObject.getValue());
				}
			}

			dataBuilder.addMetricData(UltrahumanMetricData
					.newBuilder()
					.setType(metricData.getType())
					.setObject(objectBuilder.build())
					.build());
		}

		GetMetricsResponse.Builder builder = GetMetricsResponse.newBuilder();
		builder
				.setData(dataBuilder.build())
				.setStatus(metrics.getStatus());

		if (metrics.getError() != null) {
			builder.setError(metrics.getError());
		}

		return builder.build();
	}
}
