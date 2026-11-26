package si.result.eearly.resource.grpc;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.grpc.stub.StreamObserver;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import si.result.eearly.facade.BackofficePatientServiceGrpcFacade;
import si.result.eearly.genproto.UpsertPatientDemographicsRequest;
import si.result.eearly.genproto.UpsertPatientDemographicsResponse;

class BackofficePatientGrpcControllerTest {

  @Test
  @SuppressWarnings("unchecked")
  void answersWithTheFacadeResponseAndCompletes() {
    var facade = mock(BackofficePatientServiceGrpcFacade.class);
    var request = UpsertPatientDemographicsRequest.newBuilder()
        .setUserId(UUID.randomUUID().toString()).build();
    var response = UpsertPatientDemographicsResponse.getDefaultInstance();
    when(facade.upsertPatientDemographics(request)).thenReturn(response);
    StreamObserver<UpsertPatientDemographicsResponse> observer = mock(StreamObserver.class);

    new BackofficePatientGrpcController(facade).upsertPatientDemographics(request, observer);

    verify(observer).onNext(response);
    verify(observer).onCompleted();
  }
}
