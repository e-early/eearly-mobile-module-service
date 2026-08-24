package si.result.eearly.resource.grpc;

import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;
import si.result.eearly.facade.MeasurementServiceGrpcFacade;
import si.result.eearly.genproto.CreateMeasurementRequest;
import si.result.eearly.genproto.CreateMeasurementResponse;
import si.result.eearly.genproto.GetMeasurementsForUserRequest;
import si.result.eearly.genproto.GetMeasurementsResponse;
import si.result.eearly.genproto.MeasurementServiceGrpc.MeasurementServiceImplBase;

@GrpcService
@RequiredArgsConstructor
public class MeasurementGrpcController extends MeasurementServiceImplBase {

	private final MeasurementServiceGrpcFacade measurementService;

	@Override
	public void createMeasurement(CreateMeasurementRequest request, StreamObserver<CreateMeasurementResponse> responseObserver) {
		measurementService.createBatchMeasurements(request);

		CreateMeasurementResponse response = CreateMeasurementResponse.newBuilder().build();

		responseObserver.onNext(response);
		responseObserver.onCompleted();
	}

  // TODO: discuss if this is even necessary and implement with ehrbase if needed
//	@Override
//	public void updateMeasurement(UpdateMeasurementRequest request, StreamObserver<UpdateMeasurementResponse> responseObserver) {
//		UpdateMeasurementResponse response = measurementService.updateMeasurement(request);
//
//		responseObserver.onNext(response);
//		responseObserver.onCompleted();
//	}

	@Override
	public void getMeasurements(si.result.eearly.genproto.GetMeasurementsRequest request, StreamObserver<GetMeasurementsResponse> responseObserver) {
		responseObserver.onNext(measurementService.getMeasurements(request));
		responseObserver.onCompleted();
	}

	@Override
	public void getMeasurementsForUser(GetMeasurementsForUserRequest request, StreamObserver<GetMeasurementsResponse> responseObserver) {
		responseObserver.onNext(measurementService.getMeasurementsForUser(request));
		responseObserver.onCompleted();
	}
}
