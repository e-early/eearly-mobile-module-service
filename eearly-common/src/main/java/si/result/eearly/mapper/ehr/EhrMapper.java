package si.result.eearly.mapper.ehr;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.ehrbase.openehr.sdk.serialisation.dto.DefaultValuesProvider;
import org.ehrbase.openehr.sdk.serialisation.dto.GeneratedDtoToRmConverter;
import org.ehrbase.openehr.sdk.serialisation.jsonencoding.CanonicalJson;
import org.ehrbase.openehr.sdk.serialisation.walker.defaultvalues.DefaultValues;
import si.result.eearly.domain.measurement.CreateMeasurementCommand;
import si.result.eearly.domain.measurement.MeasurementType;
import si.result.eearly.ehr.ClasspathTemplateProvider;
import si.result.eearly.ehr.EhrMedicalDevice;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public abstract class EhrMapper {
  final GeneratedDtoToRmConverter generatedDtoToRmConverter;
  final CanonicalJson canonicalJson;
  final ObjectMapper objectMapper;

  public EhrMapper() {
    DefaultValuesProvider defaultValuesProvider = ignored -> new DefaultValues() {};
    ClasspathTemplateProvider classpathTemplateProvider = new ClasspathTemplateProvider();
    this.generatedDtoToRmConverter = new GeneratedDtoToRmConverter(classpathTemplateProvider, defaultValuesProvider);

    this.canonicalJson = new CanonicalJson();
    this.objectMapper = new ObjectMapper();
  }

  OffsetDateTime stringToOffsetDateTime(String value) {
    return OffsetDateTime.parse(value, DateTimeFormatter.ofPattern ( "yyyy-MM-dd HH:mm:ss.SSSX" ));
  }

  public abstract String marshallMeasurements(List<CreateMeasurementCommand> measurementBatch, EhrMedicalDevice medicalDevice, List<MeasurementType> measurementTypes);
}
