package si.result.eearly.facade;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import si.result.eearly.domain.exception.ValidationException;
import si.result.eearly.exception.ServiceInvalidRequestException;
import si.result.eearly.genproto.MobileTokenProto.GetAllMobileTokensRequest;
import si.result.eearly.genproto.MobileTokenProto.MobileTokenResponse;
import si.result.eearly.genproto.MobileTokenProto.DeleteAllMobileTokensRequest;
import si.result.eearly.genproto.MobileTokenProto.DeleteMobileTokenRequest;
import si.result.eearly.genproto.MobileTokenProto.UpdateMobileTokenRequest;
import si.result.eearly.genproto.MobileTokenProto.CreateMobileTokenRequest;
import si.result.eearly.genproto.MobileTokenProto.MobileTokenEmptyResponse;
import si.result.eearly.mapper.MobileTokenMapper;
import si.result.eearly.service.MobileTokenService;
import si.result.eearly.service.UserPrincipalService;
import  si.result.eearly.genproto.ErrorCode;

@Service
@RequiredArgsConstructor
public class MobileTokenServiceGrpcFacade {

  private final MobileTokenService mobileTokenService;
  private final UserPrincipalService userPrincipalService;
  private final MobileTokenMapper mobileTokenMapper;

  @Transactional
  public MobileTokenEmptyResponse createMobileToken(CreateMobileTokenRequest request) {
    var userId = userPrincipalService.getUserPrincipal().getId();
    mobileTokenService.createMobileToken(mobileTokenMapper.toCreateCommand(request, userId));
    return MobileTokenEmptyResponse.newBuilder().build();
  }

  @Transactional
  public MobileTokenEmptyResponse updateMobileToken(UpdateMobileTokenRequest request) {
    var userId = userPrincipalService.getUserPrincipal().getId();
    try {
      mobileTokenService.updateMobileToken(mobileTokenMapper.toUpdateCommand(request, userId));
      return MobileTokenEmptyResponse.newBuilder().build();
    } catch (ValidationException e) {
      throw new ServiceInvalidRequestException(e.getMessage(), ErrorCode.forNumber(e.getErrorCode().getNumVal()));
    }
  }

  @Transactional
  public MobileTokenEmptyResponse deleteMobileToken(DeleteMobileTokenRequest request) {
    var userId = userPrincipalService.getUserPrincipal().getId();
    mobileTokenService.deleteMobileToken(mobileTokenMapper.toDeleteCommand(request, userId));
    return MobileTokenEmptyResponse.newBuilder().build();
  }

  @Transactional
  public MobileTokenEmptyResponse deleteAllMobileTokensForUser(DeleteAllMobileTokensRequest request) {
    var userId = userPrincipalService.getUserPrincipal().getId();
    mobileTokenService.deleteAllMobileTokens(mobileTokenMapper.toDeleteAllCommand(userId));
    return MobileTokenEmptyResponse.newBuilder().build();
  }

  @Transactional(readOnly = true)
  public MobileTokenResponse getAllMobileTokensForUser(GetAllMobileTokensRequest request) {
    var userId = userPrincipalService.getUserPrincipal().getId();
    return mobileTokenMapper.toMobileTokenResponse(mobileTokenService.getAllMobileTokensForUser(userId));
  }
}
