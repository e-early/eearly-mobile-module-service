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
import si.result.eearly.openehr.eearlyreportbloodglucosecomposition.EEARLYReportBloodGlucoseComposition;
import si.result.eearly.openehr.eearlyreportbloodglucosecomposition.definition.BloodGlucoseObservation;
import si.result.eearly.openehr.eearlyreportbloodglucosecomposition.definition.MedicalDeviceCluster;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Component("BLOOD_GLUCOSE")
public class BloodGlucoseEhrMapper extends EhrMapper {
  @Override
  public String marshallMeasurements(List<CreateMeasurementCommand> measurementBatch, EhrMedicalDevice medicalDevice, List<MeasurementType> measurementTypes) {
    EEARLYReportBloodGlucoseComposition bloodGlucoseComposition = new EEARLYReportBloodGlucoseComposition();

    bloodGlucoseComposition.setLanguage(Language.SI);
    bloodGlucoseComposition.setTerritory(Territory.SI);
    bloodGlucoseComposition.setCategoryDefiningCode(Category.EVENT);
    bloodGlucoseComposition.setSettingDefiningCode(Setting.HOME);
    bloodGlucoseComposition.setParticipations(new ArrayList<>());

    PartyRef partyRef = new PartyRef(new ObjectVersionId("test"), "si.result.eearly", "PERSON"); // TODO figure out what this is and modify
    PartySelf partySelf = new PartySelf(partyRef);
    bloodGlucoseComposition.setComposer(partySelf);




    MedicalDeviceCluster deviceCluster = null;
    if(medicalDevice != null) {
      deviceCluster = new MedicalDeviceCluster();
      var deviceId = new DvIdentifier();
      deviceId.setId(medicalDevice.id());
      deviceCluster.setUniqueDeviceIdentifierUdi(deviceId);
      deviceCluster.setDeviceNameValue(medicalDevice.name());
    }



    List<BloodGlucoseObservation> observations = new ArrayList<>();

    OffsetDateTime startTime = null;
    OffsetDateTime endTime = null;
    for (CreateMeasurementCommand measurement : measurementBatch) {

      if (startTime == null || measurement.measuredAt().isBefore(startTime)) {
        startTime = measurement.measuredAt();
      }
      if (endTime == null || measurement.measuredAt().isAfter(endTime)) {
        endTime = measurement.measuredAt();
      }

      BloodGlucoseObservation observation = new BloodGlucoseObservation();
      observation.setLanguage(Language.SI);
      observation.setSubject(new PartySelf());
      observation.setBloodGlucoseMagnitude(measurement.value());
      observation.setBloodGlucoseUnits("mmol/l");
      observation.setOriginValue(OffsetDateTime.now());
      observation.setTimeValue(measurement.measuredAt());

      if(deviceCluster != null) {
        observation.setMedicalDevice(deviceCluster);
      }

      observations.add(observation);
    }

    bloodGlucoseComposition.setStartTimeValue(startTime);
    bloodGlucoseComposition.setEndTimeValue(endTime);

    bloodGlucoseComposition.setBloodGlucose(observations);

    Composition composition = (Composition)(generatedDtoToRmConverter).toRMObject(bloodGlucoseComposition);

    return canonicalJson.marshal(composition);
  }
}
