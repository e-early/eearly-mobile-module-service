package si.result.eearly.mapper;

import org.mapstruct.CollectionMappingStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import si.result.eearly.domain.measurement.MeasurementType;
import si.result.eearly.genproto.GetMeasurementTypesResponse;

import java.util.ArrayList;
import java.util.List;

@Mapper(collectionMappingStrategy = CollectionMappingStrategy.TARGET_IMMUTABLE)
public interface MeasurementTypeMapper {

    default GetMeasurementTypesResponse toGetMeasurementTypesResponse(List<MeasurementType> measurementTypeList) {
        List<si.result.eearly.genproto.MeasurementType> measurementTypes = new ArrayList<>();
        for (MeasurementType measurementType : measurementTypeList) {
            measurementTypes.add(measurementTypeToGenprotoMeasurementType(measurementType));
        }
        return GetMeasurementTypesResponse.newBuilder().addAllMeasurementTypes(measurementTypes).build();
    }

    @Mapping(source = "id", target = "measurementTypeId")
    @Mapping(source = "measurementType", target = "measurementType")
    @Mapping(source = "measurementSubType", target = "measurementSubType")
    @Mapping(source = "minValue", target = "minValue")
    @Mapping(source = "maxValue", target = "maxValue")
    @Mapping(source = "unit", target = "unit")
    si.result.eearly.genproto.MeasurementType measurementTypeToGenprotoMeasurementType(MeasurementType measurementType);

}
