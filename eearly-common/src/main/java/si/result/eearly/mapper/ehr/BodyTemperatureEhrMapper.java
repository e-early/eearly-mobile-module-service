package si.result.eearly.mapper.ehr;

import com.nedap.archie.rm.composition.Composition;
import com.nedap.archie.rm.datavalues.DvIdentifier;
import com.nedap.archie.rm.datavalues.DvText;
import com.nedap.archie.rm.datastructures.Cluster;
import com.nedap.archie.rm.datastructures.Element;
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
import si.result.eearly.openehr.eearlyreportbodytemperaturecomposition.EEARLYReportBodyTemperatureComposition;
import si.result.eearly.openehr.eearlyreportbodytemperaturecomposition.definition.BodyTemperatureAnyEventChoice;
import si.result.eearly.openehr.eearlyreportbodytemperaturecomposition.definition.BodyTemperatureAnyEventPointEvent;
import si.result.eearly.openehr.eearlyreportbodytemperaturecomposition.definition.BodyTemperatureObservation;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Component("BODY_TEMPERATURE")
public class BodyTemperatureEhrMapper extends EhrMapper {
    @Override
    public String marshallMeasurements(List<CreateMeasurementCommand> measurementBatch, EhrMedicalDevice medicalDevice, List<MeasurementType> measurementTypes) {
        EEARLYReportBodyTemperatureComposition bodyTemperatureComposition = new EEARLYReportBodyTemperatureComposition();

        bodyTemperatureComposition.setLanguage(Language.SI);
        bodyTemperatureComposition.setTerritory(Territory.SI);
        bodyTemperatureComposition.setCategoryDefiningCode(Category.EVENT);
        bodyTemperatureComposition.setSettingDefiningCode(Setting.HOME);
        bodyTemperatureComposition.setParticipations(new ArrayList<>());

        PartyRef partyRef = new PartyRef(new ObjectVersionId("test"), "si.result.eearly", "PERSON");
        PartySelf partySelf = new PartySelf(partyRef);
        bodyTemperatureComposition.setComposer(partySelf);

        BodyTemperatureObservation observation = new BodyTemperatureObservation();
        observation.setLanguage(Language.SI);
        observation.setSubject(new PartySelf());

        if (medicalDevice != null) {
            var deviceCluster = new Cluster();
            deviceCluster.setName(new DvText("Medical device"));

            var deviceId = new DvIdentifier();
            deviceId.setId(medicalDevice.id());
            var udiElement = new Element("at0021", new DvText("Unique device identifier (UDI)"), deviceId);
            deviceCluster.addItem(udiElement);

            var deviceNameElement = new Element("at0001", new DvText("Device name"), new DvText(medicalDevice.name()));
            deviceCluster.addItem(deviceNameElement);

            observation.setDevice(deviceCluster);
        }

        List<BodyTemperatureAnyEventChoice> anyEventChoices = new ArrayList<>();
        OffsetDateTime startTime = null;
        OffsetDateTime endTime = null;

        for (CreateMeasurementCommand measurement : measurementBatch) {
            if (startTime == null || measurement.measuredAt().isBefore(startTime)) {
                startTime = measurement.measuredAt();
            }
            if (endTime == null || measurement.measuredAt().isAfter(endTime)) {
                endTime = measurement.measuredAt();
            }

            var event = new BodyTemperatureAnyEventPointEvent();
            event.setTemperatureMagnitude(measurement.value());
            event.setTemperatureUnits("Cel");
            event.setTimeValue(measurement.measuredAt());
            anyEventChoices.add(event);
        }

        bodyTemperatureComposition.setStartTimeValue(startTime);
        bodyTemperatureComposition.setEndTimeValue(endTime);

        observation.setAnyEvent(anyEventChoices);
        observation.setOriginValue(OffsetDateTime.now());

        bodyTemperatureComposition.setBodyTemperature(observation);

        Composition composition = (Composition) generatedDtoToRmConverter.toRMObject(bodyTemperatureComposition);
        return canonicalJson.marshal(composition);
    }
}