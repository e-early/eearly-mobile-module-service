package si.result.eearly.resource.grpc;

import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;
import si.result.eearly.facade.UltrahumanServiceGrpcFacade;
import si.result.eearly.genproto.GetMetricsRequest;
import si.result.eearly.genproto.GetMetricsResponse;
import si.result.eearly.genproto.UltrahumanServiceGrpc.UltrahumanServiceImplBase;

@GrpcService
@RequiredArgsConstructor
public class UltrahumanGrpcController extends UltrahumanServiceImplBase {

	private final UltrahumanServiceGrpcFacade ultrahumanService;

	@Override
	public void getMetrics(GetMetricsRequest request, StreamObserver<GetMetricsResponse> responseObserver) {
		GetMetricsResponse response = ultrahumanService.getMetrics(request);

		responseObserver.onNext(response);
		responseObserver.onCompleted();
	}
}
