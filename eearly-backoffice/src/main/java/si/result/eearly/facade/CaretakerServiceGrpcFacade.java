package si.result.eearly.facade;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import si.result.eearly.domain.exception.ValidationException;
import si.result.eearly.exception.ServiceInvalidRequestException;
import si.result.eearly.genproto.GetCaretakerResponse;
import si.result.eearly.genproto.UpsertCaretakerRequest;
import si.result.eearly.genproto.UpsertCaretakerResponse;
import si.result.eearly.mapper.CaretakerMapper;
import si.result.eearly.service.CaretakerService;
import si.result.eearly.service.FacilityService;
import si.result.eearly.service.UserPrincipalService;

@Service
@RequiredArgsConstructor
public class CaretakerServiceGrpcFacade {

    private final CaretakerService caretakerService;
    private final CaretakerMapper caretakerMapper;
    private final FacilityService facilityService;
    private final UserPrincipalService userPrincipalService;

    @Transactional
    public UpsertCaretakerResponse upsertCaretaker(UpsertCaretakerRequest request) {
        var caretakerId = request.getCaretaker().getId();
        var facility = facilityService.getFacilityById(request.getCaretaker().getFacilityId());

        try {
            return caretakerMapper.toUpsertCaretakerResponse(
                    caretakerService.upsertCaretaker(
                            caretakerMapper.toUpsertCommand(request, caretakerId, facility)));
        } catch (ValidationException e) {
            throw new ServiceInvalidRequestException(e.getMessage(), si.result.eearly.genproto.ErrorCode.forNumber(e.getErrorCode().getNumVal()));
        }

    }

    @Transactional(readOnly = true)
    public GetCaretakerResponse getCaretaker() throws ValidationException {
        String userId = userPrincipalService.getUserPrincipal().getId();
        return caretakerMapper.toGetCaretakerResponse(caretakerService.getCaretaker(userId));
    }
}
