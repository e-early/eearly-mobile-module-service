package si.result.eearly.resource.grpc;

import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;
import si.result.eearly.facade.UserDeviceServiceGrpcFacade;
import si.result.eearly.genproto.AddApiKeyRequest;
import si.result.eearly.genproto.AddApiKeyResponse;
import si.result.eearly.genproto.UserDeviceServiceGrpc;
import si.result.eearly.genproto.GetApiKeysRequest;
import si.result.eearly.genproto.GetApiKeysResponse;
import si.result.eearly.genproto.DeleteApiKeyRequest;
import si.result.eearly.genproto.DeleteApiKeyResponse;

@GrpcService
@RequiredArgsConstructor
public class UserDeviceGrpcController extends UserDeviceServiceGrpc.UserDeviceServiceImplBase {

  private final UserDeviceServiceGrpcFacade userDeviceServiceGrpcFacade;

  @Override
  public void addApiKey(AddApiKeyRequest request, StreamObserver<AddApiKeyResponse> responseObserver) {

    AddApiKeyResponse response = userDeviceServiceGrpcFacade.addApiKey(request);
    responseObserver.onNext(response);
    responseObserver.onCompleted();
  }

  public void updateApiKey(AddApiKeyRequest request, StreamObserver<AddApiKeyResponse> responseObserver) {

    AddApiKeyResponse response = userDeviceServiceGrpcFacade.updateApiKey(request);
    responseObserver.onNext(response);
    responseObserver.onCompleted();
  }

  @Override
  public void getApiKeys(GetApiKeysRequest request, StreamObserver<GetApiKeysResponse> responseObserver) {
    GetApiKeysResponse response = userDeviceServiceGrpcFacade.getApiKeys();
    responseObserver.onNext(response);
    responseObserver.onCompleted();
  }

  @Override
  public void deleteApiKey(DeleteApiKeyRequest request, StreamObserver<DeleteApiKeyResponse> responseObserver) {
    DeleteApiKeyResponse resp = userDeviceServiceGrpcFacade.deleteApiKey(request);

    responseObserver.onNext(resp);
    responseObserver.onCompleted();
  }
}
