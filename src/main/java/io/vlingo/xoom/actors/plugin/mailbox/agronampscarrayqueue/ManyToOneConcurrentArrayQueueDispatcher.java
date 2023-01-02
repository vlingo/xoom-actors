// Copyright © 2012-2023 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.actors.plugin.mailbox.agronampscarrayqueue;

import java.util.concurrent.atomic.AtomicBoolean;

import io.vlingo.xoom.actors.Backoff;
import io.vlingo.xoom.actors.Dispatcher;
import io.vlingo.xoom.actors.Mailbox;
import io.vlingo.xoom.actors.Message;

public class ManyToOneConcurrentArrayQueueDispatcher extends Thread implements Dispatcher {
  private final Backoff backoff;
  private final Mailbox mailbox;
  private final boolean requiresExecutionNotification;
  private final int throttlingCount;
  private final AtomicBoolean closed = new AtomicBoolean(false);

  protected ManyToOneConcurrentArrayQueueDispatcher(final int mailboxSize, final long fixedBackoff, final boolean notifyOnSend, final int throttlingCount, final int totalSendRetries) {
    this.backoff = fixedBackoff == 0L ? null : new Backoff(fixedBackoff);
    this.requiresExecutionNotification = fixedBackoff == 0L;
    this.mailbox = new ManyToOneConcurrentArrayQueueMailbox(this, mailboxSize, totalSendRetries, notifyOnSend);
    this.throttlingCount = throttlingCount;
  }

  @Override
  public void close() {
    closed.set(true);
  }

  @Override
  public boolean isClosed() {
    return closed.get();
  }

  @Override
  public int concurrencyCapacity() {
    return 1;
  }

  @Override
  public void execute(final Mailbox mailbox) {
    interrupt();
  }

  @Override
  public boolean requiresExecutionNotification() {
    return requiresExecutionNotification;
  }

  @Override
  public void run() {
    while (!closed.get()) {
      if (!deliver()) {
        if (backoff != null) backoff.now();
      }
    }
  }

  protected Mailbox mailbox() {
    return mailbox;
  }

  /**
   * Delivers messages in the mailbox, up to throttling count, as long as no null messages found.
   *
   * @return boolean if at least one message was delivered.
   */
  private boolean deliver() {
    for (int idx = 0; idx < throttlingCount; ++idx) {
      final Message message = mailbox.receive();
      if (message == null) {
        return idx > 0; // we delivered at least one message
      } else {
        message.deliver();
      }
    }
    return true;
  }
}
