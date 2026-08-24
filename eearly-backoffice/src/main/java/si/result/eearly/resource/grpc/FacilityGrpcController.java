package si.result.eearly.resource.grpc;

import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;
import si.result.eearly.facade.FacilityServiceGrpcFacade;
import si.result.eearly.genproto.*;

@GrpcService
@RequiredArgsConstructor
public class FacilityGrpcController extends FacilityServiceGrpc.FacilityServiceImplBase {

    private final FacilityServiceGrpcFacade facilityService;

    @Override
    public void upsertFacility(UpsertFacilityRequest request, StreamObserver<UpsertFacilityResponse> responseObserver) {
        UpsertFacilityResponse response = facilityService.upsertFacility(request);
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void getFacilities(GetFacilitiesRequestEmpty request, StreamObserver<GetFacilitiesResponse> responseObserver) {
        final var response = facilityService.getAllFacilities();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
