package si.result.eearly.resource.grpc;

import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;
import si.result.eearly.facade.DexcomServiceGrpcFacade;
import si.result.eearly.genproto.DexcomServiceGrpc.DexcomServiceImplBase;
import si.result.eearly.genproto.GetAlertsRequest;
import si.result.eearly.genproto.GetAlertsResponse;
import si.result.eearly.genproto.GetCalibrationsRequest;
import si.result.eearly.genproto.GetCalibrationsResponse;
import si.result.eearly.genproto.GetEgvsRequest;
import si.result.eearly.genproto.GetEgvsResponse;
import si.result.eearly.genproto.GetDataRangeRequest;
import si.result.eearly.genproto.GetDataRangeResponse;
import si.result.eearly.genproto.GetDevicesRequest;
import si.result.eearly.genproto.GetDevicesResponse;
import si.result.eearly.genproto.GetEventsRequest;
import si.result.eearly.genproto.GetEventsResponse;

@GrpcService
@RequiredArgsConstructor
public class DexcomGrpcController extends DexcomServiceImplBase {

	private final DexcomServiceGrpcFacade dexcomService;

	@Override
	public void getAlerts(GetAlertsRequest request, StreamObserver<GetAlertsResponse> responseObserver) {
		GetAlertsResponse response = dexcomService.getAlerts(request);

		responseObserver.onNext(response);
		responseObserver.onCompleted();
	}

	@Override
	public void getCalibrations(GetCalibrationsRequest request,
								StreamObserver<GetCalibrationsResponse> responseObserver) {
		GetCalibrationsResponse response = dexcomService.getCalibrations(request);

		responseObserver.onNext(response);
		responseObserver.onCompleted();
	}

	@Override
	public void getEgvs(GetEgvsRequest request, StreamObserver<GetEgvsResponse> responseObserver) {
		GetEgvsResponse response = dexcomService.getEgvs(request);

		responseObserver.onNext(response);
		responseObserver.onCompleted();
	}

	@Override
	public void getDataRange(GetDataRangeRequest request, StreamObserver<GetDataRangeResponse> responseObserver) {
		GetDataRangeResponse response = dexcomService.getDataRange(request);

		responseObserver.onNext(response);
		responseObserver.onCompleted();
	}

	@Override
	public void getDevices(GetDevicesRequest request, StreamObserver<GetDevicesResponse> responseObserver) {
		GetDevicesResponse response = dexcomService.getDevices(request);

		responseObserver.onNext(response);
		responseObserver.onCompleted();
	}

	@Override
	public void getEvents(GetEventsRequest request, StreamObserver<GetEventsResponse> responseObserver) {
		GetEventsResponse response = dexcomService.getEvents(request);

		responseObserver.onNext(response);
		responseObserver.onCompleted();
	}
}
