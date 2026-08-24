package si.result.eearly.mapper;

import org.mapstruct.Mapper;
import si.result.eearly.domain.facility.Facility;
import si.result.eearly.domain.facility.UpsertFacilityCommand;
import si.result.eearly.genproto.UpsertFacilityRequest;
import si.result.eearly.genproto.UpsertFacilityResponse;


@Mapper
public interface FacilityMapper {

    default UpsertFacilityCommand toUpsertCommand(UpsertFacilityRequest request) {
        var upsertFacilityCommand = new UpsertFacilityCommand(
                request.getFacility().getId(),
                request.getFacility().getName(),
                request.getFacility().getDescription());
        return upsertFacilityCommand;
    }

    UpsertFacilityResponse toUpsertFacilityResponse(Facility facility);

    si.result.eearly.genproto.Facility toFacilityProto(Facility facility);

    default si.result.eearly.genproto.GetFacilitiesResponse toGetFacilitiesResponse(java.util.List<Facility> facilities) {
        return si.result.eearly.genproto.GetFacilitiesResponse.newBuilder()
                .addAllFacilities(facilities.stream().map(this::toFacilityProto).toList())
                .build();
    }
}
