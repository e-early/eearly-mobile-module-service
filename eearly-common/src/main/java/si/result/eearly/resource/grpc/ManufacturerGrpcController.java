package si.result.eearly.resource.grpc;

import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;
import si.result.eearly.facade.ManufacturerServiceGrpcFacade;
import si.result.eearly.genproto.ManufacturerProto.GetManufacturersResponse;
import si.result.eearly.genproto.ManufacturerProto.GetManufacturersRequestEmpty;
import si.result.eearly.genproto.ManufacturerServiceGrpc;


@GrpcService
@RequiredArgsConstructor
public class ManufacturerGrpcController extends ManufacturerServiceGrpc.ManufacturerServiceImplBase {

    private final ManufacturerServiceGrpcFacade manufacturerService;

    @Override
    public void getManufacturers(GetManufacturersRequestEmpty request, StreamObserver<GetManufacturersResponse> responseObserver) {
        final var response = manufacturerService.getAllManufacturers();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
