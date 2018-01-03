// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors.plugin.mailbox.sharedringbuffer;

import io.vlingo.actors.Backoff;
import io.vlingo.actors.Dispatcher;
import io.vlingo.actors.Mailbox;
import io.vlingo.actors.Message;

import java.util.concurrent.atomic.AtomicBoolean;

public class RingBufferDispatcher extends Thread implements Dispatcher {
  private final Backoff backoff;
  private final AtomicBoolean closed = new AtomicBoolean(false);
  private final Mailbox mailbox;
  private final boolean requiresExecutionNotification;
  private final int throttlingCount;

  public void close() {
    closed.set(true);
  }

  public void execute(final Mailbox mailbox) {
    interrupt();
  }

  public boolean requiresExecutionNotification() {
    return requiresExecutionNotification;
  }

  @Override
  public void run() {
    while (!closed.get()) {
      if (!deliver()) {
        backoff.now();
      }
    }
  }

  protected RingBufferDispatcher(final int mailboxSize, final long fixedBackoff, final int throttlingCount) {
    this.backoff = fixedBackoff == 0L ? new Backoff() : new Backoff(fixedBackoff);
    this.requiresExecutionNotification = fixedBackoff == 0L;
    this.mailbox = new SharedRingBufferMailbox(this, mailboxSize);
    this.throttlingCount = throttlingCount;
  }

  protected Mailbox mailbox() {
    return mailbox;
  }

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
