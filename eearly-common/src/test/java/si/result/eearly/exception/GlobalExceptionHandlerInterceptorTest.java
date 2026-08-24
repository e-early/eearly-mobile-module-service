package si.result.eearly.exception;


import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.Status;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import si.result.eearly.domain.exception.DeviceUserAlreadyExistsException;
import si.result.eearly.domain.exception.NotFoundException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import si.result.eearly.genproto.ErrorCode;

class GlobalExceptionHandlerInterceptorTest {

  @Mock
  private ServerCall<Object, Object> serverCall;

  @Mock
  private ServerCallHandler<Object, Object> serverCallHandler;

  @Mock
  private Metadata metadata;

  @Mock
  private ServerCall.Listener<Object> mockListener;

  @InjectMocks
  private GlobalExceptionHandlerInterceptor interceptor;

  @Captor
  private ArgumentCaptor<Status> statusCaptor;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);

    // Mock the behavior to return a valid listener
    when(serverCallHandler.startCall(any(), any())).thenReturn(mockListener);
  }

  @Test
  void interceptCall_ShouldHandleDeviceUserAlreadyExistsException() {
    // Arrange
    doThrow(new DeviceUserAlreadyExistsException("Device User already exists")).when(mockListener).onMessage(any());

    ServerCall.Listener<Object> listener = interceptor.interceptCall(serverCall, metadata, serverCallHandler);

    // Act
    listener.onMessage(new Object());

    // Assert
    verify(serverCall).close(statusCaptor.capture(), any(Metadata.class));
    assertEquals(Status.ALREADY_EXISTS.getCode(), statusCaptor.getValue().getCode());
  }

  @Test
  void interceptCall_ShouldHandleDeviceUserNotFoundException() {
    // Arrange
    doThrow(new NotFoundException("Device User not found")).when(mockListener).onMessage(any());

    ServerCall.Listener<Object> listener = interceptor.interceptCall(serverCall, metadata, serverCallHandler);

    // Act
    listener.onMessage(new Object());

    // Assert
    verify(serverCall).close(statusCaptor.capture(), any(Metadata.class));
    assertEquals(Status.NOT_FOUND.getCode(), statusCaptor.getValue().getCode());
  }

  @Test
  void interceptCall_ShouldHandleServiceInvalidRequestException() {
    // Arrange
    ServiceInvalidRequestException exception = new ServiceInvalidRequestException("Invalid request", ErrorCode.forNumber(ErrorCode.VALIDATION_ERROR.getNumber()));
    doThrow(exception).when(mockListener).onMessage(any());

    ServerCall.Listener<Object> listener = interceptor.interceptCall(serverCall, metadata, serverCallHandler);

    // Act
    listener.onMessage(new Object());

    // Assert
    verify(serverCall).close(statusCaptor.capture(), any(Metadata.class));
    assertEquals(Status.INVALID_ARGUMENT.getCode(), statusCaptor.getValue().getCode());
  }

  @Test
  void interceptCall_ShouldHandleUnknownException() {
    // Arrange
    doThrow(new RuntimeException("Unknown error")).when(mockListener).onMessage(any());

    ServerCall.Listener<Object> listener = interceptor.interceptCall(serverCall, metadata, serverCallHandler);

    // Act
    listener.onMessage(new Object());

    // Assert
    verify(serverCall).close(statusCaptor.capture(), any(Metadata.class));
    assertEquals(Status.INTERNAL.getCode(), statusCaptor.getValue().getCode());
  }
}