package si.result.eearly.resource.grpc;

import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;
import si.result.eearly.facade.ApiDeviceGrpcFacade;
import si.result.eearly.genproto.CheckHeartbeatRequest;
import si.result.eearly.genproto.CheckHeartbeatResponse;
import si.result.eearly.genproto.GetDexcomAuthorizationUriRequest;
import si.result.eearly.genproto.GetDexcomAuthorizationUriResponse;
import si.result.eearly.genproto.GetMeasurementRequest;
import si.result.eearly.genproto.GetMeasurementResponse;
import si.result.eearly.genproto.ApiDeviceServiceGrpc.ApiDeviceServiceImplBase;

@GrpcService
@RequiredArgsConstructor
public class ApiDeviceGrpcController extends ApiDeviceServiceImplBase {

	private final ApiDeviceGrpcFacade deviceService;

	@Override
	public void checkHeartbeat(CheckHeartbeatRequest request, StreamObserver<CheckHeartbeatResponse> responseObserver) {
		CheckHeartbeatResponse response = deviceService.checkHeartbeat(request);

		responseObserver.onNext(response);
		responseObserver.onCompleted();
	}

	@Override
	public void getMeasurement(GetMeasurementRequest request, StreamObserver<GetMeasurementResponse> responseObserver) {
		GetMeasurementResponse response = deviceService.getMeasurement(request);

		responseObserver.onNext(response);
		responseObserver.onCompleted();
	}

	@Override
	public void getDexcomAuthorizationUri(
			GetDexcomAuthorizationUriRequest request,
			StreamObserver<GetDexcomAuthorizationUriResponse> responseObserver) {
		GetDexcomAuthorizationUriResponse response = deviceService.getDexcomAuthorizationUri(request);

		responseObserver.onNext(response);
		responseObserver.onCompleted();
	}
}
