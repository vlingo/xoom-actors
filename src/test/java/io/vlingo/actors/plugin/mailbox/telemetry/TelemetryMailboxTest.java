package io.vlingo.actors.plugin.mailbox.telemetry;

import io.vlingo.actors.Mailbox;
import io.vlingo.actors.Message;
import org.junit.Before;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.*;

public class TelemetryMailboxTest {
  private MailboxTelemetry telemetry;
  private Mailbox delegate;
  private Mailbox telemetryMailbox;
  private Message message;

  @Before
  public void setUp() {
    telemetry = mock(MailboxTelemetry.class);
    delegate = mock(Mailbox.class);
    message = mock(Message.class);

    telemetryMailbox = new TelemetryMailbox(telemetry, delegate);
  }

  @Test
  public void testThatSendingAMessageIsTracked() {
    telemetryMailbox.send(message);

    verify(delegate).send(message);
    verify(telemetry).onSendMessage(message);
  }

  @Test
  public void testThatFailingSendAMessageIsTracked() {
    final Throwable expectedException = new IllegalStateException("Expected exception ocurred");
    doThrow(expectedException).when(delegate).send(message);

    telemetryMailbox.send(message);

    verify(delegate).send(message);
    verify(telemetry).onSendMessageFailed(message, expectedException);
    verify(telemetry, never()).onSendMessage(message);
  }

  @Test
  public void testThatPullingAnEmptyMailboxIsTracked() {
    doReturn(null).when(delegate).receive();

    Message nullMessage = telemetryMailbox.receive();

    verify(delegate).receive();
    verify(telemetry).onPullEmptyMailbox();
    verify(telemetry, never()).onPulledMessage(null);

    assertNull(nullMessage);
  }

  @Test
  public void testThatPullingANotEmptyMailboxIsTracked() {
    doReturn(message).when(delegate).receive();

    Message notNullMessage = telemetryMailbox.receive();

    verify(delegate).receive();
    verify(telemetry).onPulledMessage(message);
    verify(telemetry, never()).onPullEmptyMailbox();

    assertEquals(message, notNullMessage);
  }

  @Test(expected = IllegalStateException.class)
  public void testThatFailingPullingAMailboxIsTracked() {
    final Throwable expectedException = new IllegalStateException("Expected exception ocurred");
    doThrow(expectedException).when(delegate).receive();

    try {
      telemetryMailbox.receive();
    } finally {
      verify(delegate).receive();
      verify(telemetry).onPullMessageFailed(expectedException);
      verify(telemetry, never()).onPulledMessage(any());
      verify(telemetry, never()).onPullEmptyMailbox();
    }
  }

  @Test
  public void testThatCloseIsDelegated() {
    telemetryMailbox.close();
    verify(delegate).close();
  }

  @Test
  public void testThatIsClosedIsDelegated() {
    boolean closed = new Random().nextBoolean();

    doReturn(closed).when(delegate).isClosed();
    boolean result = telemetryMailbox.isClosed();

    verify(delegate).isClosed();
    assertEquals(closed, result);
  }

  @Test
  public void testThatIsDeliveringIsDelegated() {
    boolean delivering = new Random().nextBoolean();

    doReturn(delivering).when(delegate).isDelivering();
    boolean result = telemetryMailbox.isDelivering();

    verify(delegate).isDelivering();
    assertEquals(delivering, result);
  }

  @Test
  public void testThatDeliveringIsDelegated() {
    boolean delivering = new Random().nextBoolean();

    doReturn(delivering).when(delegate).delivering(delivering);
    boolean result = telemetryMailbox.delivering(delivering);

    verify(delegate).delivering(delivering);
    assertEquals(delivering, result);
  }

  @Test
  public void testThatRunIsDelegated() {
    telemetryMailbox.run();
    verify(delegate).run();
  }
}
