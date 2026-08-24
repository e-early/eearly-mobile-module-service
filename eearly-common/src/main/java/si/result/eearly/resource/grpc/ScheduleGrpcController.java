package si.result.eearly.resource.grpc;

import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;
import si.result.eearly.facade.ScheduleServiceGrpcFacade;
import si.result.eearly.genproto.ScheduleServiceGrpc;
import si.result.eearly.genproto.SendMeasurementReminderRequest;
import si.result.eearly.genproto.SendMeasurementReminderResponse;
import si.result.eearly.genproto.SendScheduleNotificationRequest;
import si.result.eearly.genproto.SendScheduleNotificationResponse;
import si.result.eearly.genproto.GetScheduleEntriesResponse;
import si.result.eearly.genproto.GetScheduleEntriesRequest;
import si.result.eearly.genproto.GetSchedulesRequest;
import si.result.eearly.genproto.GetSchedulesResponse;

@GrpcService
@RequiredArgsConstructor
public class ScheduleGrpcController extends ScheduleServiceGrpc.ScheduleServiceImplBase {
  private final ScheduleServiceGrpcFacade scheduleServiceGrpcFacade;

  @Override
  public void getSchedules(GetSchedulesRequest request, StreamObserver<GetSchedulesResponse> responseObserver) {
    GetSchedulesResponse response = scheduleServiceGrpcFacade.getSchedules(request.getFilter());
    responseObserver.onNext(response);
    responseObserver.onCompleted();
  }

  @Override
  public void getScheduleEntries(GetScheduleEntriesRequest request, StreamObserver<GetScheduleEntriesResponse> responseObserver) {
    GetScheduleEntriesResponse response = scheduleServiceGrpcFacade.getScheduleEntries(request.getFilter());

    responseObserver.onNext(response);
    responseObserver.onCompleted();
  }

  @Override
  public void sendScheduleNotification(SendScheduleNotificationRequest request,
      StreamObserver<SendScheduleNotificationResponse> responseObserver) {
    SendScheduleNotificationResponse response = scheduleServiceGrpcFacade
        .sendScheduleNotificationResponse(request.getScheduleId());

    responseObserver.onNext(response);
    responseObserver.onCompleted();
  }

  @Override
  public void sendMeasurementReminder(
      SendMeasurementReminderRequest request,
      StreamObserver<SendMeasurementReminderResponse> responseObserver) {
    SendMeasurementReminderResponse response = scheduleServiceGrpcFacade.sendMeasurementReminder(
        request.getScheduleId(),
        request.getScheduledAt(),
        request.getScheduleName());

    responseObserver.onNext(response);
    responseObserver.onCompleted();
  }
}
