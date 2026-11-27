package si.result.eearly.exception;

import io.grpc.*;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.interceptor.GrpcGlobalServerInterceptor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import si.result.eearly.domain.exception.DeviceUserAlreadyExistsException;
import si.result.eearly.domain.exception.NotFoundException;
import si.result.eearly.genproto.Error;


@GrpcGlobalServerInterceptor
@Component
@Slf4j
public class GlobalExceptionHandlerInterceptor implements ServerInterceptor {

  @Override
  public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> serverCall,
                                                               Metadata requestHeaders,
                                                               ServerCallHandler<ReqT, RespT> serverCallHandler) {
    try {
      ServerCall.Listener<ReqT> delegate = serverCallHandler.startCall(serverCall, requestHeaders);
      return new ForwardingServerCallListener.SimpleForwardingServerCallListener<ReqT>(delegate) {
        @Override
        public void onMessage(ReqT message) {
          try {
            super.onMessage(message); // onNext called
          } catch (Exception e) {
            logUnexpected(e);
            handleEndpointException(e, serverCall);
          }
        }

        @Override
        public void onHalfClose() {
          try {
            super.onHalfClose(); // onCompleted called
          } catch (Exception e) {
            logUnexpected(e);
            handleEndpointException(e, serverCall);
          }
        }
      };
    } catch (Exception e) {
      log.error("Error |", e);
      return handleInterceptorException(e, serverCall);
    }
  }

  private <ReqT, RespT> void handleEndpointException(Throwable t, ServerCall<ReqT, RespT> serverCall) {
    if (!(t instanceof AccessDeniedException)) {
      log.error("An exception occurred in the endpoint implementation. ", t);
    }

    switch (t) {

      case DeviceUserAlreadyExistsException deviceUserAlreadyExistsException -> {
        log.error("Closing server call with status ALREADY_EXISTS: {}", t.getMessage());
        serverCall.close(Status.ALREADY_EXISTS.withCause(t).withDescription(t.getMessage()), new Metadata());
      }
      case NotFoundException notFoundException-> {
        log.error("Closing server call with status NOT_FOUND: {}", t.getMessage());
        serverCall.close(Status.NOT_FOUND.withCause(t).withDescription(t.getMessage()), new Metadata());
      }
      case ServiceInvalidRequestException serviceInvalidRequestException -> {
        var errorDetails = Error.newBuilder().setCode(serviceInvalidRequestException.getErrorCode()).setMessage(t.getMessage()).build();
        log.error("Closing server call with status INVALID_ARGUMENT and error details: {}", errorDetails);
        serverCall.close(Status.INVALID_ARGUMENT.withCause(t).withDescription(t.getMessage()), new Metadata());
      }
      case AlreadyExistsException alreadyExistsException -> {
        var errorDetails = Error.newBuilder().setCode(alreadyExistsException.getErrorCode()).setMessage(t.getMessage()).build();
        log.error("Closing server call with status INVALID_ARGUMENT and error details: {}", errorDetails);
        serverCall.close(Status.ALREADY_EXISTS.withCause(t).withDescription(t.getMessage()), new Metadata());
      }
      case AccessDeniedException accessDeniedException -> {
        // A client problem, not a server fault: no stack trace, and no internal detail returned.
        log.warn("Closing server call with status PERMISSION_DENIED: {}", t.getMessage());
        serverCall.close(Status.PERMISSION_DENIED.withCause(t).withDescription("Access denied"),
            new Metadata());
      }
      default -> {
        log.error("Closing server call with status INTERNAL: {}", t.getMessage());
        serverCall.close(Status.INTERNAL.withCause(t).withDescription(t.getMessage()), new Metadata());
      }
    }
  }

  /** Access denials are expected client errors; they are logged once, at WARN, when handled. */
  private static void logUnexpected(Exception e) {
    if (!(e instanceof AccessDeniedException)) {
      log.error("Error |", e);
    }
  }

  private <ReqT, RespT> ServerCall.Listener<ReqT> handleInterceptorException(Throwable t, ServerCall<ReqT, RespT> serverCall) {
    log.error("An exception occurred in a **subsequent** interceptor. An exception occurred in the endpoint implementation ", t);
    serverCall.close(Status.INTERNAL, new Metadata());
    return new ServerCall.Listener<ReqT>() {
      // no-op
    };
  }
}
