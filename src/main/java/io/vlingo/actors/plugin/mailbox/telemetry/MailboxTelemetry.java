package io.vlingo.actors.plugin.mailbox.telemetry;

import io.vlingo.actors.Message;

public interface MailboxTelemetry {
  void onSendMessage(final Message message);
  void onSendMessageFailed(final Message message, final Throwable exception);

  void onPullEmptyMailbox();
  void onPulledMessage(final Message message);
  void onPullMessageFailed(final Throwable exception);
}
