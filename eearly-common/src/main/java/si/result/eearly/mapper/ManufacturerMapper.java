package si.result.eearly.mapper;

import org.mapstruct.CollectionMappingStrategy;
import org.mapstruct.Mapper;
import si.result.eearly.domain.manufacturer.Manufacturer;
import si.result.eearly.genproto.ManufacturerProto;
import si.result.eearly.genproto.ManufacturerProto.GetManufacturersResponse;

import java.util.ArrayList;
import java.util.List;

@Mapper(collectionMappingStrategy = CollectionMappingStrategy.TARGET_IMMUTABLE)
public interface ManufacturerMapper {

    default GetManufacturersResponse toGetManufacturersResponse(List<Manufacturer> manufacturers) {
        List<ManufacturerProto.Manufacturer> manufacturerList = new ArrayList<>();
        for (Manufacturer manufacturer : manufacturers) {
            manufacturerList.add(toManufacturerProto(manufacturer));
        }
        return GetManufacturersResponse.newBuilder().addAllManufacturers(manufacturerList).build();
    }

    ManufacturerProto.Manufacturer toManufacturerProto(Manufacturer manufacturer);
}
