package si.result.eearly.resource.grpc;

import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;
import si.result.eearly.facade.BackofficeScheduleServiceGrpcFacade;
import si.result.eearly.genproto.*;
import si.result.eearly.genproto.BackofficeScheduleServiceGrpc.BackofficeScheduleServiceImplBase;

@GrpcService
@RequiredArgsConstructor
public class BackofficeScheduleGrpcController extends BackofficeScheduleServiceImplBase {
  private final BackofficeScheduleServiceGrpcFacade scheduleServiceGrpcFacade;

  @Override
  public void upsertSchedule(UpsertScheduleRequest request, StreamObserver<UpsertScheduleResponse> responseObserver) {
    UpsertScheduleResponse response = scheduleServiceGrpcFacade.upsertSchedule(request);
    responseObserver.onNext(response);
    responseObserver.onCompleted();
  }

  @Override
  public void createUpdateSchedule(CreateUpdateScheduleRequest request, StreamObserver<GetScheduleResponse> responseObserver) {
    GetScheduleResponse response = scheduleServiceGrpcFacade.createUpdateSchedule(request);
    responseObserver.onNext(response);
    responseObserver.onCompleted();
  }

  @Override
  public void createUpdateScheduleEntry(CreateUpdateScheduleEntryRequest request, StreamObserver<GetScheduleEntriesResponse> responseObserver) {
    GetScheduleEntriesResponse response = scheduleServiceGrpcFacade.createUpdateScheduleEntry(request);
    responseObserver.onNext(response);
    responseObserver.onCompleted();
  }

  @Override
  public void addUserToSchedule(AddUserToScheduleRequest request, StreamObserver<GetScheduleResponse> responseObserver) {
    GetScheduleResponse response = scheduleServiceGrpcFacade.addUserToSchedule(request);
    responseObserver.onNext(response);
    responseObserver.onCompleted();
  }

  @Override
  public void removeUserFromSchedule(RemoveUserFromScheduleRequest request, StreamObserver<GetScheduleEmptyResponse> responseObserver) {
    GetScheduleEmptyResponse response = scheduleServiceGrpcFacade.removeUserFromSchedule(request);
    responseObserver.onNext(response);
    responseObserver.onCompleted();
  }

  @Override
  public void getSchedulesForCaretaker(GetSchedulesForCaretakerRequest request, StreamObserver<GetSchedulesResponse> responseObserver) {
    GetSchedulesResponse response = scheduleServiceGrpcFacade.getSchedulesForCaretaker(request.getFilter());
    responseObserver.onNext(response);
    responseObserver.onCompleted();
  }

  @Override
  public void getScheduleEntriesForCaretaker(GetScheduleEntriesForCaretakerRequest request, StreamObserver<GetScheduleEntriesResponse> responseObserver) {
    GetScheduleEntriesResponse response = scheduleServiceGrpcFacade.getScheduleEntriesForCaretaker(request.getScheduleId(), request.getFilter());
    responseObserver.onNext(response);
    responseObserver.onCompleted();
  }

  @Override
  public void deleteScheduleEntry(DeleteScheduleEntryRequest request, StreamObserver<GetScheduleResponse> responseObserver) {
    GetScheduleResponse response = scheduleServiceGrpcFacade.deleteScheduleEntry(request.getScheduleId(), request.getScheduleEntryId());
    responseObserver.onNext(response);
    responseObserver.onCompleted();
  }

  @Override
  public void deleteSchedule(DeleteScheduleRequest request, StreamObserver<DeleteScheduleResponse> responseObserver) {
    DeleteScheduleResponse response = scheduleServiceGrpcFacade.deleteSchedule(request);
    responseObserver.onNext(response);
    responseObserver.onCompleted();
  }
}
