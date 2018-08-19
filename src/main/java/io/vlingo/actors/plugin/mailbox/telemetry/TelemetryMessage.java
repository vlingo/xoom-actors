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
    telemetry.onPulledMessage(this);
    try {
      delegate.deliver();
    } catch (RuntimeException ex) {
      telemetry.onDeliverMessageFailed(this, ex);
    }
  }

  @Override
  public String representation() {
    return null;
  }

  @Override
  public boolean isStowed() {
    return false;
  }
}
