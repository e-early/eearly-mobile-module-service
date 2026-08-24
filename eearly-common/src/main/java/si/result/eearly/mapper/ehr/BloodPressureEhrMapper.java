package si.result.eearly.mapper.ehr;

import com.nedap.archie.rm.composition.Composition;
import com.nedap.archie.rm.datavalues.DvIdentifier;
import com.nedap.archie.rm.generic.PartySelf;
import com.nedap.archie.rm.support.identification.ObjectVersionId;
import com.nedap.archie.rm.support.identification.PartyRef;
import lombok.extern.slf4j.Slf4j;
import org.ehrbase.openehr.sdk.generator.commons.shareddefinition.Category;
import org.ehrbase.openehr.sdk.generator.commons.shareddefinition.Language;
import org.ehrbase.openehr.sdk.generator.commons.shareddefinition.Setting;
import org.ehrbase.openehr.sdk.generator.commons.shareddefinition.Territory;
import org.springframework.stereotype.Component;
import si.result.eearly.domain.enums.MeasurementSubTypeEnum;
import si.result.eearly.domain.measurement.CreateMeasurementCommand;
import si.result.eearly.domain.measurement.MeasurementType;
import si.result.eearly.ehr.EhrMedicalDevice;
import si.result.eearly.openehr.eearlyreportbloodpressurecomposition.EEARLYReportBloodPressureComposition;
import si.result.eearly.openehr.eearlyreportbloodpressurecomposition.definition.BloodPressureAnyEventChoice;
import si.result.eearly.openehr.eearlyreportbloodpressurecomposition.definition.BloodPressureAnyEventPointEvent;
import si.result.eearly.openehr.eearlyreportbloodpressurecomposition.definition.BloodPressureObservation;
import si.result.eearly.openehr.eearlyreportbloodpressurecomposition.definition.MedicalDeviceCluster;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Component("BLOOD_PRESSURE")
@Slf4j
public class BloodPressureEhrMapper extends EhrMapper {
  @Override
  public String marshallMeasurements(List<CreateMeasurementCommand> measurementBatch, EhrMedicalDevice medicalDevice, List<MeasurementType> measurementTypes) {
    EEARLYReportBloodPressureComposition bloodPressureComposition = new EEARLYReportBloodPressureComposition();

    bloodPressureComposition.setLanguage(Language.SI);
    bloodPressureComposition.setTerritory(Territory.SI);
    bloodPressureComposition.setCategoryDefiningCode(Category.EVENT);
    bloodPressureComposition.setSettingDefiningCode(Setting.HOME);
    bloodPressureComposition.setParticipations(new ArrayList<>());

    PartyRef partyRef = new PartyRef(new ObjectVersionId("test"), "si.result.eearly", "PERSON"); // TODO figure out what this is and modify
    PartySelf partySelf = new PartySelf(partyRef);
    bloodPressureComposition.setComposer(partySelf);

    BloodPressureObservation observation = new BloodPressureObservation();
    observation.setLanguage(Language.SI);
    observation.setSubject(new PartySelf());

    if(medicalDevice != null) {
      var deviceCluster = new MedicalDeviceCluster();
      var deviceId = new DvIdentifier();
      deviceId.setId(medicalDevice.id());
      deviceCluster.setUniqueDeviceIdentifierUdi(deviceId);
      deviceCluster.setDeviceNameValue(medicalDevice.name());
      observation.setMedicalDevice(deviceCluster);
    }

    List<BloodPressureAnyEventChoice> bloodPressureAnyEventChoices = new ArrayList<>();


    var bloodPressureEvent = new BloodPressureAnyEventPointEvent();

    List<String> groupIdsAlreadyInserted = new ArrayList<>();
    OffsetDateTime startTime = null;
    OffsetDateTime endTime = null;
    for (CreateMeasurementCommand measurement : measurementBatch) {
      String groupId = String.valueOf(measurement.measurementGroupId());
      if(groupIdsAlreadyInserted.contains(groupId)) {
        continue;
      }
      groupIdsAlreadyInserted.add(groupId);

      if (startTime == null || measurement.measuredAt().isBefore(startTime)) {
        startTime = measurement.measuredAt();
      }
      if (endTime == null || measurement.measuredAt().isAfter(endTime)) {
        endTime = measurement.measuredAt();
      }

      bloodPressureEvent.setDiastolicUnits("mm[Hg]");
      bloodPressureEvent.setSystolicUnits("mm[Hg]");
      MeasurementType measurementType = measurementTypes.stream().filter(mt -> Objects.equals(mt.getId().toString(), measurement.measurementTypeId())).findAny().orElse(null);
      if(measurementType == null) {
        log.error("Could not determine measurement type with id {}", measurement.measurementTypeId());
        throw new RuntimeException("Could not determine measurement type with id " + measurement.measurementTypeId());
      }

      List<CreateMeasurementCommand> measurementsWithSameGroupId = measurementBatch.stream().filter(m ->
                      String.valueOf(m.measurementGroupId()).equals(groupId)).toList();
      if(measurementsWithSameGroupId.size() > 2) {
        throw new IllegalArgumentException("More than two blood pressure measurements exist with same group id of " + groupId);
      }
      // get systolic measurement if the current measurement is diastolic and vice versa
      CreateMeasurementCommand otherMeasurement = measurementsWithSameGroupId.stream().filter(m ->
                      !m.measurementTypeId().equals(measurement.measurementTypeId()))
              .findFirst().orElse(null);

      if(measurementType.getMeasurementSubType() == MeasurementSubTypeEnum.DIASTOLIC) {
        bloodPressureEvent.setDiastolicMagnitude(measurement.value());
        if(otherMeasurement != null) {
          bloodPressureEvent.setSystolicMagnitude(otherMeasurement.value());
        }
      } else {
        bloodPressureEvent.setSystolicMagnitude(measurement.value());
        if(otherMeasurement != null) {
          bloodPressureEvent.setDiastolicMagnitude(otherMeasurement.value());
        }
      }
      bloodPressureEvent.setTimeValue(measurement.measuredAt());
      bloodPressureAnyEventChoices.add(bloodPressureEvent);
    }

    bloodPressureComposition.setStartTimeValue(startTime);
    bloodPressureComposition.setEndTimeValue(endTime);

    observation.setAnyEvent(bloodPressureAnyEventChoices);
    observation.setOriginValue(OffsetDateTime.now());

    bloodPressureComposition.setBloodPressure(List.of(observation));

    Composition composition = (Composition)(generatedDtoToRmConverter).toRMObject(bloodPressureComposition);

    return canonicalJson.marshal(composition);
  }
}
