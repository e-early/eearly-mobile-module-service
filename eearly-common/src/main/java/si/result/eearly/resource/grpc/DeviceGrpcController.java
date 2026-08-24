package si.result.eearly.resource.grpc;

import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;
import si.result.eearly.facade.DeviceServiceGrpcFacade;
import si.result.eearly.genproto.DeviceServiceGrpc;
import si.result.eearly.genproto.GetSupportedDeviceRequest;
import si.result.eearly.genproto.GetSupportedDeviceResponse;
import si.result.eearly.genproto.GetSupportedDevicesForManufacturerRequest;
import si.result.eearly.genproto.GetSupportedDevicesForManufacturerResponse;


@GrpcService
@RequiredArgsConstructor
public class DeviceGrpcController extends DeviceServiceGrpc.DeviceServiceImplBase {

    private final DeviceServiceGrpcFacade deviceService;

    @Override
    public void getSupportedDevice(GetSupportedDeviceRequest request, StreamObserver<GetSupportedDeviceResponse> responseObserver) {
        final var resp = deviceService.getSupportedDevice(request.getDeviceId());

        responseObserver.onNext(resp);
        responseObserver.onCompleted();
    }

    @Override
    public void getSupportedDevicesForManufacturer(GetSupportedDevicesForManufacturerRequest request, StreamObserver<GetSupportedDevicesForManufacturerResponse> responseObserver) {
        final var resp = deviceService.getSupportedDevicesForManufacturer(request.getManufacturerId());

        responseObserver.onNext(resp);
        responseObserver.onCompleted();
    }
}
