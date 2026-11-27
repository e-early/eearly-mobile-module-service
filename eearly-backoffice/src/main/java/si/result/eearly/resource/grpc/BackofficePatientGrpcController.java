package si.result.eearly.resource.grpc;

import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;
import si.result.eearly.facade.BackofficePatientServiceGrpcFacade;
import si.result.eearly.genproto.BackofficePatientServiceGrpc.BackofficePatientServiceImplBase;
import si.result.eearly.genproto.UpsertPatientDemographicsRequest;
import si.result.eearly.genproto.UpsertPatientDemographicsResponse;

@GrpcService
@RequiredArgsConstructor
public class BackofficePatientGrpcController extends BackofficePatientServiceImplBase {
  private final BackofficePatientServiceGrpcFacade patientServiceGrpcFacade;

  @Override
  public void upsertPatientDemographics(UpsertPatientDemographicsRequest request,
      StreamObserver<UpsertPatientDemographicsResponse> responseObserver) {
    responseObserver.onNext(patientServiceGrpcFacade.upsertPatientDemographics(request));
    responseObserver.onCompleted();
  }
}
