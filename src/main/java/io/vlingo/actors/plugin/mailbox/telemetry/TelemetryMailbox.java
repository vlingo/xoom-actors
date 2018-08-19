package io.vlingo.actors.plugin.mailbox.telemetry;

import io.vlingo.actors.Mailbox;
import io.vlingo.actors.Message;

public class TelemetryMailbox implements Mailbox {
  private final MailboxTelemetry telemetry;
  private final Mailbox delegate;

  public TelemetryMailbox(final MailboxTelemetry telemetry, final Mailbox delegate) {
    this.telemetry = telemetry;
    this.delegate = delegate;
  }

  @Override
  public void close() {
    delegate.close();
  }

  @Override
  public boolean isClosed() {
    return delegate.isClosed();
  }

  @Override
  public boolean isDelivering() {
    return delegate.isDelivering();
  }

  @Override
  public boolean delivering(final boolean flag) {
    return delegate.delivering(flag);
  }

  @Override
  public void send(final Message message) {
    try {
      delegate.send(message);
      telemetry.onSendMessage(message);
    } catch (Exception e) {
      telemetry.onSendMessageFailed(message, e);
    }
  }

  @Override
  public Message receive() {
    try {
      Message receivedMessage = delegate.receive();
      if (receivedMessage == null) {
        telemetry.onPullEmptyMailbox();
      } else {
        telemetry.onPulledMessage(receivedMessage);
      }

      return receivedMessage;
    } catch (RuntimeException e) {
      telemetry.onPullMessageFailed(e);
      throw e;
    }
  }

  @Override
  public void run() {
    for (;;) {
      final Message message = receive();
      if (message != null) {
        message.deliver();
      } else {
        break;
      }
    }
  }
}
