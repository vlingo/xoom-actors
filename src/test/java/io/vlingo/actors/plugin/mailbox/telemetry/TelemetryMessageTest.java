package io.vlingo.actors.plugin.mailbox.telemetry;

import io.vlingo.actors.Actor;
import io.vlingo.actors.Message;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class TelemetryMessageTest {
  private Message delegate;
  private MailboxTelemetry telemetry;
  private TelemetryMessage telemetryMessage;

  @Before
  public void setUp() throws Exception {
    delegate = mock(Message.class);
    telemetry = mock(MailboxTelemetry.class);

    telemetryMessage = new TelemetryMessage(delegate, telemetry);
  }

  @Test
  public void testThatActorIsDelegated() {
    final Actor actor = mock(Actor.class);
    doReturn(actor).when(delegate).actor();

    final Actor result = telemetryMessage.actor();

    verify(delegate).actor();
    assertEquals(actor, result);
  }

  @Test
  public void testThatDeliverIsTrackedByTelemetry() {
    telemetryMessage.deliver();

    verify(delegate).deliver();
    verify(telemetry).onPulledMessage(delegate);
  }

  @Test
  public void testThatDeliverErrorsAreTrackedByTelemetry() {
    final IllegalStateException exception = new IllegalStateException("Expected exception");
    doThrow(exception).when(delegate).deliver();

    telemetryMessage.deliver();

    verify(delegate).deliver();
    verify(telemetry, never()).onPulledMessage(delegate);
    verify(telemetry).onDeliverMessageFailed(delegate, exception);
  }
}