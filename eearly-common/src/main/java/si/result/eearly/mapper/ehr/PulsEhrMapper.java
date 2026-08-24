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
import si.result.eearly.openehr.eearlyreportpulsecomposition.EEARLYReportPulseComposition;
import si.result.eearly.openehr.eearlyreportpulsecomposition.definition.MedicalDeviceCluster;
import si.result.eearly.openehr.eearlyreportpulsecomposition.definition.PulsAnyEventChoice;
import si.result.eearly.openehr.eearlyreportpulsecomposition.definition.PulsAnyEventPointEvent;
import si.result.eearly.openehr.eearlyreportpulsecomposition.definition.PulsObservation;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Component("HEART_RATE")
public class PulsEhrMapper extends EhrMapper {

  @Override
  public String marshallMeasurements(List<CreateMeasurementCommand> measurementBatch, EhrMedicalDevice medicalDevice,List<MeasurementType> measurementTypes) {
    EEARLYReportPulseComposition pulsComposition = new EEARLYReportPulseComposition();

    pulsComposition.setLanguage(Language.SI);
    pulsComposition.setTerritory(Territory.SI);
    pulsComposition.setCategoryDefiningCode(Category.EVENT);
    pulsComposition.setSettingDefiningCode(Setting.HOME);
    pulsComposition.setParticipations(new ArrayList<>());

    PartyRef partyRef = new PartyRef(new ObjectVersionId("test"), "si.result.eearly", "PERSON"); // TODO figure out what this is and modify
    PartySelf partySelf = new PartySelf(partyRef);
    pulsComposition.setComposer(partySelf);

    PulsObservation observation = new PulsObservation();
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

    List<PulsAnyEventChoice> pulsAnyEventChoices = new ArrayList<>();

    OffsetDateTime startTime = null;
    OffsetDateTime endTime = null;
    for (CreateMeasurementCommand measurement : measurementBatch) {

      if (startTime == null || measurement.measuredAt().isBefore(startTime)) {
        startTime = measurement.measuredAt();
      }
      if (endTime == null || measurement.measuredAt().isAfter(endTime)) {
        endTime = measurement.measuredAt();
      }

      var pulsEvent = new PulsAnyEventPointEvent();
      pulsEvent.setRateMagnitude(measurement.value());

      pulsEvent.setTimeValue(measurement.measuredAt());
      pulsEvent.setRateUnits("/min");

      pulsAnyEventChoices.add(pulsEvent);
    }

    pulsComposition.setStartTimeValue(startTime);
    pulsComposition.setEndTimeValue(endTime);

    observation.setAnyEvent(pulsAnyEventChoices);
    observation.setOriginValue(OffsetDateTime.now());

    pulsComposition.setPuls(observation);

    Composition composition = (Composition)(generatedDtoToRmConverter).toRMObject(pulsComposition);

    return canonicalJson.marshal(composition);
  }
}
