package si.result.eearly.service;

import org.springframework.transaction.annotation.Transactional;
import si.result.eearly.domain.exception.ValidationException;
import si.result.eearly.domain.token.CreateMobileTokenCommand;
import si.result.eearly.domain.token.DeleteAllMobileTokensCommand;
import si.result.eearly.domain.token.DeleteMobileTokenCommand;
import si.result.eearly.domain.token.MobileToken;
import si.result.eearly.domain.token.UpdateMobileTokenCommand;

import java.util.List;

public interface MobileTokenService {

    void createMobileToken(CreateMobileTokenCommand command);

    void deleteMobileToken(DeleteMobileTokenCommand command);

    @Transactional
    void deleteAllMobileTokens(DeleteAllMobileTokensCommand command);

    @Transactional
    void updateMobileToken(UpdateMobileTokenCommand command) throws ValidationException;

    List<MobileToken> getAllMobileTokensForUser(String userId);
}
