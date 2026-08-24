package si.result.eearly.facade;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import si.result.eearly.domain.exception.ValidationException;
import si.result.eearly.exception.ServiceInvalidRequestException;
import si.result.eearly.genproto.ErrorCode;
import si.result.eearly.genproto.GetFacilitiesResponse;
import si.result.eearly.genproto.UpsertFacilityRequest;
import si.result.eearly.genproto.UpsertFacilityResponse;
import si.result.eearly.mapper.FacilityMapper;
import si.result.eearly.service.FacilityService;

@Service
@RequiredArgsConstructor
public class FacilityServiceGrpcFacade {

    private final FacilityService facilityService;
    private final FacilityMapper facilityMapper;

    @Transactional(readOnly = true)
    public GetFacilitiesResponse getAllFacilities() {
        return facilityMapper.toGetFacilitiesResponse(facilityService.getAllFacilities());
    }

    @Transactional()
    public UpsertFacilityResponse upsertFacility(UpsertFacilityRequest request) {
        try {
            return facilityMapper.toUpsertFacilityResponse(
                    facilityService.upsertFacility(
                            facilityMapper.toUpsertCommand(request)));
        } catch (ValidationException e) {
            throw new ServiceInvalidRequestException(e.getMessage(), ErrorCode.forNumber(e.getErrorCode().getNumVal()));
        }
    }
}
