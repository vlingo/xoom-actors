package io.vlingo.actors.plugin.mailbox.telemetry;

import io.vlingo.actors.Dispatcher;
import io.vlingo.actors.Mailbox;
import io.vlingo.actors.MailboxProvider;

public class TelemetryMailboxProvider implements MailboxProvider {
  private final MailboxTelemetry telemetry;
  private final MailboxProvider delegate;

  public TelemetryMailboxProvider(final MailboxTelemetry telemetry, final MailboxProvider delegate) {
    this.telemetry = telemetry;
    this.delegate = delegate;
  }

  @Override
  public void close() {
    delegate.close();
  }

  @Override
  public Mailbox provideMailboxFor(final int hashCode) {
    return new TelemetryMailbox(telemetry, delegate.provideMailboxFor(hashCode));
  }

  @Override
  public Mailbox provideMailboxFor(final int hashCode, final Dispatcher dispatcher) {
    return new TelemetryMailbox(telemetry, delegate.provideMailboxFor(hashCode, dispatcher));
  }
}
