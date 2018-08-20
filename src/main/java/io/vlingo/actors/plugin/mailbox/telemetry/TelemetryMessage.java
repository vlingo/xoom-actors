package io.vlingo.actors.plugin.mailbox.telemetry;

import io.vlingo.actors.Actor;
import io.vlingo.actors.Message;

public class TelemetryMessage implements Message {
  private final Message delegate;
  private final MailboxTelemetry telemetry;

  public TelemetryMessage(final Message delegate, final MailboxTelemetry telemetry) {
    this.delegate = delegate;
    this.telemetry = telemetry;
  }

  @Override
  public Actor actor() {
    return delegate.actor();
  }

  @Override
  public void deliver() {
    try {
      delegate.deliver();
      telemetry.onPulledMessage(delegate);
    } catch (RuntimeException ex) {
      telemetry.onDeliverMessageFailed(delegate, ex);
    }
  }

  @Override
  public String representation() {
    return delegate.representation();
  }

  @Override
  public boolean isStowed() {
    return delegate.isStowed();
  }
}
