package si.result.eearly.mapper.ehr;

import com.nedap.archie.rm.composition.Composition;
import com.nedap.archie.rm.datavalues.DvIdentifier;
import com.nedap.archie.rm.generic.PartySelf;
import com.nedap.archie.rm.support.identification.ObjectVersionId;
import com.nedap.archie.rm.support.identification.PartyRef;
import org.ehrbase.openehr.sdk.generator.commons.shareddefinition.Category;
import org.ehrbase.openehr.sdk.generator.commons.shareddefinition.Language;
import org.ehrbase.openehr.sdk.generator.commons.shareddefinition.Setting;
import org.ehrbase.openehr.sdk.generator.commons.shareddefinition.Territory;
import org.springframework.stereotype.Component;
import si.result.eearly.domain.measurement.CreateMeasurementCommand;
import si.result.eearly.domain.measurement.MeasurementType;
import si.result.eearly.ehr.EhrMedicalDevice;
import si.result.eearly.openehr.eearlyreportbodyheightcomposition.EEARLYReportBodyHeightComposition;
import si.result.eearly.openehr.eearlyreportbodyheightcomposition.definition.HeightLengthAnyEventChoice;
import si.result.eearly.openehr.eearlyreportbodyheightcomposition.definition.HeightLengthAnyEventPointEvent;
import si.result.eearly.openehr.eearlyreportbodyheightcomposition.definition.HeightLengthObservation;
import si.result.eearly.openehr.eearlyreportbodyheightcomposition.definition.MedicalDeviceCluster;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Component("BODY_HEIGHT")
public class BodyHeightEhrMapper extends EhrMapper {
  @Override
  public String marshallMeasurements(List<CreateMeasurementCommand> measurementBatch, EhrMedicalDevice medicalDevice, List<MeasurementType> measurementTypes) {
    EEARLYReportBodyHeightComposition bodyHeightComposition = new EEARLYReportBodyHeightComposition();

    bodyHeightComposition.setLanguage(Language.SI);
    bodyHeightComposition.setTerritory(Territory.SI);
    bodyHeightComposition.setCategoryDefiningCode(Category.EVENT);
    bodyHeightComposition.setSettingDefiningCode(Setting.HOME);
    bodyHeightComposition.setParticipations(new ArrayList<>());

    PartyRef partyRef = new PartyRef(new ObjectVersionId("test"), "si.result.eearly", "PERSON"); // TODO figure out what this is and modify
    PartySelf partySelf = new PartySelf(partyRef);
    bodyHeightComposition.setComposer(partySelf);

    HeightLengthObservation observation = new HeightLengthObservation();
    observation.setLanguage(Language.SI);
    observation.setSubject(new PartySelf());

    if(medicalDevice != null) {
      MedicalDeviceCluster deviceCluster = new MedicalDeviceCluster();
      var deviceId = new DvIdentifier();
      deviceId.setId(medicalDevice.id());
      deviceCluster.setUniqueDeviceIdentifierUdi(deviceId);
      deviceCluster.setDeviceNameValue(medicalDevice.name());
      observation.setMedicalDevice(deviceCluster);
    }

    List<HeightLengthAnyEventChoice> heightLengthAnyEventChoices = new ArrayList<>();

    OffsetDateTime startTime = null;
    OffsetDateTime endTime = null;
    for (CreateMeasurementCommand measurement : measurementBatch) {

      if (startTime == null || measurement.measuredAt().isBefore(startTime)) {
        startTime = measurement.measuredAt();
      }
      if (endTime == null || measurement.measuredAt().isAfter(endTime)) {
        endTime = measurement.measuredAt();
      }

      var heightLengthEvenet = new HeightLengthAnyEventPointEvent();
      heightLengthEvenet.setHeightLengthMagnitude(measurement.value());
      heightLengthEvenet.setTimeValue(measurement.measuredAt());
      heightLengthEvenet.setHeightLengthUnits("cm");

      heightLengthAnyEventChoices.add(heightLengthEvenet);
    }

    bodyHeightComposition.setStartTimeValue(startTime);
    bodyHeightComposition.setEndTimeValue(endTime);

    observation.setAnyEvent(heightLengthAnyEventChoices);
    observation.setOriginValue(OffsetDateTime.now());

    bodyHeightComposition.setHeightLength(List.of(observation));

    Composition composition = (Composition)(generatedDtoToRmConverter).toRMObject(bodyHeightComposition);

    return canonicalJson.marshal(composition);
  }
}
