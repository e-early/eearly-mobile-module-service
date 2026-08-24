package si.result.eearly.resource.grpc;

import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;
import si.result.eearly.facade.ProfileServiceGrpcFacade;
import si.result.eearly.genproto.ProfileServiceGrpc;
import si.result.eearly.genproto.SendFCMNotificationForTokenRequest;
import si.result.eearly.genproto.SendFCMNotificationForTokenResponse;
import si.result.eearly.genproto.UpsertProfileRequest;
import si.result.eearly.genproto.UpsertProfileResponse;
import si.result.eearly.genproto.UpdateUsernameResponse;
import si.result.eearly.genproto.UpdateUsernameRequest;
import si.result.eearly.genproto.GetProfileRequest;
import si.result.eearly.genproto.GetProfileResponse;

@GrpcService
@RequiredArgsConstructor
public class ProfileGrpcController extends ProfileServiceGrpc.ProfileServiceImplBase {

	private final ProfileServiceGrpcFacade profileService;

	@Override
	public void upsertProfile(UpsertProfileRequest request, StreamObserver<UpsertProfileResponse> responseObserver) {
		UpsertProfileResponse response = profileService.upsertProfile(request);
		responseObserver.onNext(response);
		responseObserver.onCompleted();
	}

	@Override
	public void getProfile(GetProfileRequest request, StreamObserver<GetProfileResponse> responseObserver) {
		GetProfileResponse response = profileService.getProfile();
		responseObserver.onNext(response);
		responseObserver.onCompleted();
	}

	@Override
	public void updateUsername(UpdateUsernameRequest request, StreamObserver<UpdateUsernameResponse> responseObserver) {
		UpdateUsernameResponse response = profileService.updateUsername(request);

		responseObserver.onNext(response);
		responseObserver.onCompleted();
	}

	@Override
	public void sendFCMNotificationForToken(SendFCMNotificationForTokenRequest request, StreamObserver<si.result.eearly.genproto.SendFCMNotificationForTokenResponse> responseObserver) {
		SendFCMNotificationForTokenResponse response = profileService.sendNotificationForToken(request);
		responseObserver.onNext(response);
		responseObserver.onCompleted();
	}
}
