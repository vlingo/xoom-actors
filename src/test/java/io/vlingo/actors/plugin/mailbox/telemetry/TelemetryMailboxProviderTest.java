package io.vlingo.actors.plugin.mailbox.telemetry;

import io.vlingo.actors.Dispatcher;
import io.vlingo.actors.Mailbox;
import io.vlingo.actors.MailboxProvider;
import org.junit.Before;
import org.junit.Test;

import java.util.UUID;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.*;

public class TelemetryMailboxProviderTest {
  private Mailbox delegateMailbox;
  private MailboxTelemetry telemetry;
  private MailboxProvider delegate;
  private MailboxProvider telemetryMailboxProvider;
  private Dispatcher dispatcher;

  @Before
  public void setUp() {
    delegateMailbox = mock(Mailbox.class);
    delegate = mock(MailboxProvider.class);
    telemetry = mock(MailboxTelemetry.class);
    dispatcher = mock(Dispatcher.class);

    telemetryMailboxProvider = new TelemetryMailboxProvider(telemetry, delegate);

    doReturn(delegateMailbox).when(delegate).provideMailboxFor(anyInt());
    doReturn(delegateMailbox).when(delegate).provideMailboxFor(anyInt(), any());
  }

  @Test
  public void testThatCloseIsDelegated() {
    telemetryMailboxProvider.close();
    verify(delegate).close();
  }

  @Test
  public void testThatAssignedMailboxIsWrappedWithTelemetryMailbox() {
    final int hashCode = randomHashCode();
    Mailbox mailbox = telemetryMailboxProvider.provideMailboxFor(hashCode);

    verify(delegate).provideMailboxFor(hashCode);
    assertTrue(mailbox instanceof TelemetryMailbox);

    mailbox.close();
    verify(delegateMailbox).close();
  }

  @Test
  public void testThatAssignedMailboxIsWrappedWithTelemetryMailboxWhenWeHaveBothHashcodeAndDispatcher() {
    final int hashCode = randomHashCode();
    Mailbox mailbox = telemetryMailboxProvider.provideMailboxFor(hashCode, dispatcher);

    verify(delegate).provideMailboxFor(hashCode, dispatcher);
    assertTrue(mailbox instanceof TelemetryMailbox);

    mailbox.close();
    verify(delegateMailbox).close();
  }

  private int randomHashCode() {
    return UUID.randomUUID().toString().hashCode();
  }
}
