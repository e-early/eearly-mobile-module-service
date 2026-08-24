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
import si.result.eearly.openehr.eearlyreportpulseoximetrycomposition.EEARLYReportPulseOximetryComposition;
import si.result.eearly.openehr.eearlyreportpulseoximetrycomposition.definition.MedicalDeviceCluster;
import si.result.eearly.openehr.eearlyreportpulseoximetrycomposition.definition.PulseOximetryAnyEventChoice;
import si.result.eearly.openehr.eearlyreportpulseoximetrycomposition.definition.PulseOximetryAnyEventPointEvent;
import si.result.eearly.openehr.eearlyreportpulseoximetrycomposition.definition.PulseOximetryObservation;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Component("OXYGEN_SATURATION")
public class BloodOxygenEhrMapper extends EhrMapper {
  @Override
  public String marshallMeasurements(List<CreateMeasurementCommand> measurementBatch, EhrMedicalDevice medicalDevice, List<MeasurementType> measurementTypes) {
    EEARLYReportPulseOximetryComposition pulseOximetryComposition = new EEARLYReportPulseOximetryComposition();

    pulseOximetryComposition.setLanguage(Language.SI);
    pulseOximetryComposition.setTerritory(Territory.SI);
    pulseOximetryComposition.setCategoryDefiningCode(Category.EVENT);
    pulseOximetryComposition.setSettingDefiningCode(Setting.HOME);
    pulseOximetryComposition.setParticipations(new ArrayList<>());

    PartyRef partyRef = new PartyRef(new ObjectVersionId("test"), "si.result.eearly", "PERSON"); // TODO figure out what this is and modify
    PartySelf partySelf = new PartySelf(partyRef);
    pulseOximetryComposition.setComposer(partySelf);

    PulseOximetryObservation observation = new PulseOximetryObservation();
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


    List<PulseOximetryAnyEventChoice> pulseOximetryAnyEventChoice = new ArrayList<>();

    OffsetDateTime startTime = null;
    OffsetDateTime endTime = null;
    for (CreateMeasurementCommand measurement : measurementBatch) {

      if (startTime == null || measurement.measuredAt().isBefore(startTime)) {
        startTime = measurement.measuredAt();
      }
      if (endTime == null || measurement.measuredAt().isAfter(endTime)) {
        endTime = measurement.measuredAt();
      }

      var pulseOximetryEvent = new PulseOximetryAnyEventPointEvent();
      pulseOximetryEvent.setSpocMagnitude(measurement.value());

      pulseOximetryEvent.setTimeValue(measurement.measuredAt());
      pulseOximetryEvent.setSpocUnits("ml/dl");

      pulseOximetryAnyEventChoice.add(pulseOximetryEvent);
    }

    pulseOximetryComposition.setStartTimeValue(startTime);
    pulseOximetryComposition.setEndTimeValue(endTime);

    observation.setAnyEvent(pulseOximetryAnyEventChoice);
    observation.setOriginValue(OffsetDateTime.now());

    pulseOximetryComposition.setPulseOximetry(List.of(observation));

    Composition composition = (Composition)(generatedDtoToRmConverter).toRMObject(pulseOximetryComposition);

    return canonicalJson.marshal(composition);
  }
}
