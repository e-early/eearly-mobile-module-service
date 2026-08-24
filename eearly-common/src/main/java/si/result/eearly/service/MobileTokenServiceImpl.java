package si.result.eearly.service;

import ch.qos.logback.core.util.StringUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import si.result.eearly.domain.exception.ErrorCode;
import si.result.eearly.domain.exception.ValidationException;
import si.result.eearly.domain.token.CreateMobileTokenCommand;
import si.result.eearly.domain.token.DeleteAllMobileTokensCommand;
import si.result.eearly.domain.token.DeleteMobileTokenCommand;
import si.result.eearly.domain.token.MobileToken;
import si.result.eearly.domain.token.UpdateMobileTokenCommand;
import si.result.eearly.repository.MobileTokenRepository;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class MobileTokenServiceImpl implements MobileTokenService {

	private final MobileTokenRepository mobileTokenRepository;

	@Override
	public void createMobileToken(CreateMobileTokenCommand command) {
		mobileTokenRepository.save(MobileToken.create(command.token(), command.userId()));
	}

	@Override
	public void deleteMobileToken(DeleteMobileTokenCommand command) {
		mobileTokenRepository.deleteByTokenAndUserId(command.token(), UUID.fromString(command.userId()));
	}

	@Override
	public void deleteAllMobileTokens(DeleteAllMobileTokensCommand command) {
		mobileTokenRepository.deleteByUserId(UUID.fromString(command.userId()));
	}

	@Override
	public void updateMobileToken(UpdateMobileTokenCommand command) throws ValidationException {

		if (StringUtil.isNullOrEmpty(command.oldToken())) {
			throw new ValidationException(ErrorCode.VALIDATION_ERROR, "oldToken must not be empty");
		}

		if (mobileTokenRepository.existsMobileTokenByTokenAndUserId(command.oldToken(), UUID.fromString(command.userId()))) {

			if (StringUtil.isNullOrEmpty(command.newToken())) {
				throw new ValidationException(ErrorCode.VALIDATION_ERROR, "newToken must not be empty");
			}

			mobileTokenRepository.deleteById(command.oldToken());
			mobileTokenRepository.save(MobileToken.create(command.newToken(), command.userId()));
		} else {
			throw new ValidationException(ErrorCode.VALIDATION_ERROR, "oldToken does not exists or is used by another user");
		}
	}

	@Override
	public List<MobileToken> getAllMobileTokensForUser(String userId) {
		return mobileTokenRepository.findAllByUserId(UUID.fromString(userId));
	}
}
