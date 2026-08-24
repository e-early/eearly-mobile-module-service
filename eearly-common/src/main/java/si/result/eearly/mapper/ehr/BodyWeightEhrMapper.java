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
import si.result.eearly.openehr.eearlyreportbodyweightcomposition.EEARLYReportBodyWeightComposition;
import si.result.eearly.openehr.eearlyreportbodyweightcomposition.definition.BodyWeightAnyEventChoice;
import si.result.eearly.openehr.eearlyreportbodyweightcomposition.definition.BodyWeightAnyEventPointEvent;
import si.result.eearly.openehr.eearlyreportbodyweightcomposition.definition.BodyWeightObservation;
import si.result.eearly.openehr.eearlyreportbodyweightcomposition.definition.MedicalDeviceCluster;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Component("BODY_WEIGHT")
public class BodyWeightEhrMapper extends EhrMapper {
  @Override
  public String marshallMeasurements(List<CreateMeasurementCommand> measurementBatch, EhrMedicalDevice medicalDevice, List<MeasurementType> measurementTypes) {
    EEARLYReportBodyWeightComposition bodyWeightComposition = new EEARLYReportBodyWeightComposition();

    bodyWeightComposition.setLanguage(Language.SI);
    bodyWeightComposition.setTerritory(Territory.SI);
    bodyWeightComposition.setCategoryDefiningCode(Category.EVENT);
    bodyWeightComposition.setSettingDefiningCode(Setting.HOME);
    bodyWeightComposition.setParticipations(new ArrayList<>());

    PartyRef partyRef = new PartyRef(new ObjectVersionId("test"), "si.result.eearly", "PERSON"); // TODO figure out what this is and modify if needed
    PartySelf partySelf = new PartySelf(partyRef);
    bodyWeightComposition.setComposer(partySelf);

    BodyWeightObservation observation = new BodyWeightObservation();
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

    List<BodyWeightAnyEventChoice> anyEventChoices = new ArrayList<>();

    OffsetDateTime startTime = null;
    OffsetDateTime endTime = null;
    for (CreateMeasurementCommand measurement : measurementBatch) {

      if (startTime == null || measurement.measuredAt().isBefore(startTime)) {
        startTime = measurement.measuredAt();
      }
      if (endTime == null || measurement.measuredAt().isAfter(endTime)) {
        endTime = measurement.measuredAt();
      }

      var bodyWeightEvent = new BodyWeightAnyEventPointEvent();
      bodyWeightEvent.setWeightMagnitude(measurement.value());
      bodyWeightEvent.setTimeValue(measurement.measuredAt());
      bodyWeightEvent.setWeightUnits("kg");

      anyEventChoices.add(bodyWeightEvent);
    }

    bodyWeightComposition.setStartTimeValue(startTime);
    bodyWeightComposition.setEndTimeValue(endTime);

    observation.setAnyEvent(anyEventChoices);
    observation.setOriginValue(OffsetDateTime.now());

    bodyWeightComposition.setBodyWeight(List.of(observation));

    Composition composition = (Composition) (generatedDtoToRmConverter).toRMObject(bodyWeightComposition);

    return canonicalJson.marshal(composition);
  }
}
