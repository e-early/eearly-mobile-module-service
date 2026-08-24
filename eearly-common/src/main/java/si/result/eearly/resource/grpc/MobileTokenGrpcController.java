package si.result.eearly.resource.grpc;

import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;
import si.result.eearly.facade.MobileTokenServiceGrpcFacade;
import si.result.eearly.genproto.MobileTokenProto.MobileTokenResponse;
import si.result.eearly.genproto.MobileTokenProto.GetAllMobileTokensRequest;
import si.result.eearly.genproto.MobileTokenProto.UpdateMobileTokenRequest;
import si.result.eearly.genproto.MobileTokenProto.DeleteAllMobileTokensRequest;
import si.result.eearly.genproto.MobileTokenProto.DeleteMobileTokenRequest;
import si.result.eearly.genproto.MobileTokenProto.MobileTokenEmptyResponse;
import si.result.eearly.genproto.MobileTokenProto.CreateMobileTokenRequest;
import si.result.eearly.genproto.MobileTokenServiceGrpc.MobileTokenServiceImplBase;

@GrpcService
@RequiredArgsConstructor
public class MobileTokenGrpcController extends MobileTokenServiceImplBase {

	private final MobileTokenServiceGrpcFacade mobileTokenServiceGrpcFacade;

	@Override
	public void createMobileToken(CreateMobileTokenRequest request, StreamObserver<MobileTokenEmptyResponse> responseObserver) {
		MobileTokenEmptyResponse response = mobileTokenServiceGrpcFacade.createMobileToken(request);
		responseObserver.onNext(response);
		responseObserver.onCompleted();
	}

	@Override
	public void updateMobileToken(UpdateMobileTokenRequest request, StreamObserver<MobileTokenEmptyResponse> responseObserver) {
		MobileTokenEmptyResponse response = mobileTokenServiceGrpcFacade.updateMobileToken(request);
		responseObserver.onNext(response);
		responseObserver.onCompleted();
	}

	@Override
	public void deleteMobileToken(DeleteMobileTokenRequest request, StreamObserver<MobileTokenEmptyResponse> responseObserver) {
		MobileTokenEmptyResponse response = mobileTokenServiceGrpcFacade.deleteMobileToken(request);
		responseObserver.onNext(response);
		responseObserver.onCompleted();
	}

	@Override
	public void deleteAllMobileToken(DeleteAllMobileTokensRequest request, StreamObserver<MobileTokenEmptyResponse> responseObserver) {
		MobileTokenEmptyResponse response = mobileTokenServiceGrpcFacade.deleteAllMobileTokensForUser(request);
		responseObserver.onNext(response);
		responseObserver.onCompleted();
	}

	@Override
	public void getAllMobileTokens(GetAllMobileTokensRequest request, StreamObserver<MobileTokenResponse> responseObserver) {
		MobileTokenResponse response = mobileTokenServiceGrpcFacade.getAllMobileTokensForUser(request);
		responseObserver.onNext(response);
		responseObserver.onCompleted();
	}
}
