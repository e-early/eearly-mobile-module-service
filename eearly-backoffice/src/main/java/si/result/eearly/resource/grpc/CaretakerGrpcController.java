package si.result.eearly.resource.grpc;

import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;
import si.result.eearly.domain.exception.ValidationException;
import si.result.eearly.exception.ServiceInvalidRequestException;
import si.result.eearly.facade.CaretakerServiceGrpcFacade;
import si.result.eearly.genproto.*;

@GrpcService
@RequiredArgsConstructor
public class CaretakerGrpcController extends CaretakerServiceGrpc.CaretakerServiceImplBase {

    private final CaretakerServiceGrpcFacade caretakerService;

    @Override
    public void getCaretaker(GetCaretakerEmptyRequest request, StreamObserver<GetCaretakerResponse> responseObserver) {
        try {
            final var response = caretakerService.getCaretaker();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (ValidationException e) {
            throw new ServiceInvalidRequestException(e.getMessage(), si.result.eearly.genproto.ErrorCode.forNumber(e.getErrorCode().getNumVal()));
        }
    }

    @Override
    public void upsertCaretaker(UpsertCaretakerRequest request, StreamObserver<UpsertCaretakerResponse> responseObserver) {
        UpsertCaretakerResponse response = caretakerService.upsertCaretaker(request);
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
