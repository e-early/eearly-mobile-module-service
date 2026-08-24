package si.result.eearly.service.firebase;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import si.result.eearly.exception.FirebaseNotificationException;

@ExtendWith(MockitoExtension.class)
class FirebaseServiceImplTest {

  @Mock
  private FirebaseMessaging firebaseMessaging;

  @InjectMocks
  private FirebaseServiceImpl firebaseService;


  @Test
  void testSendNotificationForToken_Success() throws FirebaseMessagingException {
    // Arrange
    String token = "dummyToken";
    String title = "TestTitle";
    String body = "TestBody";
    String expectedMessageId = "messageId";

    // Mock
    Mockito.when(firebaseMessaging.send(ArgumentMatchers.any(Message.class))).thenReturn(expectedMessageId);

    // Act
    String fcmReturnMsg = firebaseService.sendNotificationForToken(token, title, body);

    // Verify
    Mockito.verify(firebaseMessaging, Mockito.times(1)).send(ArgumentMatchers.any(Message.class));
    Assertions.assertEquals(expectedMessageId, fcmReturnMsg);

  }

  @Test
  void testSendNotificationForToken_Failure() throws FirebaseMessagingException {
    // Arrange
    String token = "dummyToken";
    String title = "TestTitle";
    String body = "TestBody";

    // Mock the FirebaseMessagingException to be thrown
    Mockito.when(firebaseMessaging.send(ArgumentMatchers.any(Message.class))).thenThrow(Mockito.mock(FirebaseMessagingException.class));

    // Act & Assert
    FirebaseNotificationException exception = Assertions.assertThrows(FirebaseNotificationException.class, () ->
        firebaseService.sendNotificationForToken(token, title, body));

    // Verify exception message
    Mockito.verify(firebaseMessaging, Mockito.times(1)).send(ArgumentMatchers.any(Message.class));
  }

}