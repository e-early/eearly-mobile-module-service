package si.result.eearly.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import si.result.eearly.domain.token.CreateMobileTokenCommand;
import si.result.eearly.domain.token.DeleteAllMobileTokensCommand;
import si.result.eearly.domain.token.DeleteMobileTokenCommand;
import si.result.eearly.domain.token.MobileToken;
import si.result.eearly.domain.token.UpdateMobileTokenCommand;
import si.result.eearly.genproto.MobileTokenProto.MobileTokenResponse;
import si.result.eearly.genproto.MobileTokenProto.DeleteMobileTokenRequest;
import si.result.eearly.genproto.MobileTokenProto.UpdateMobileTokenRequest;
import si.result.eearly.genproto.MobileTokenProto.CreateMobileTokenRequest;

import java.util.List;
import java.util.stream.Collectors;

@Mapper
public interface MobileTokenMapper {

    CreateMobileTokenCommand toCreateCommand(CreateMobileTokenRequest request, String userId);

    UpdateMobileTokenCommand toUpdateCommand(UpdateMobileTokenRequest request, String userId);

    DeleteMobileTokenCommand toDeleteCommand(DeleteMobileTokenRequest request, String userId);

    DeleteAllMobileTokensCommand toDeleteAllCommand(String userId);

    default MobileTokenResponse toMobileTokenResponse(List<MobileToken> mobileTokens) {
        List<String> tokens = mobileTokens.stream()
                .map(MobileToken::getToken)
                .collect(Collectors.toList());
        return MobileTokenResponse.newBuilder().addAllTokens(tokens).build();
    }

}
