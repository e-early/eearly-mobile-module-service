package si.result.eearly.resource.grpc;

import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;
import si.result.eearly.facade.MeasurementTypeServiceGrpcFacade;
import si.result.eearly.genproto.GetMeasurementTypesRequestEmpty;
import si.result.eearly.genproto.GetMeasurementTypesResponse;
import si.result.eearly.genproto.MeasurementTypeServiceGrpc;

@GrpcService
@RequiredArgsConstructor
public class MeasurementTypeGrpcController extends MeasurementTypeServiceGrpc.MeasurementTypeServiceImplBase {

	private final MeasurementTypeServiceGrpcFacade measurementTypeService;

	@Override
	public void getMeasurementTypes(GetMeasurementTypesRequestEmpty request, StreamObserver<GetMeasurementTypesResponse> responseObserver) {
		GetMeasurementTypesResponse response = measurementTypeService.getMeasurementTypes();
		responseObserver.onNext(response);
		responseObserver.onCompleted();
	}
}
