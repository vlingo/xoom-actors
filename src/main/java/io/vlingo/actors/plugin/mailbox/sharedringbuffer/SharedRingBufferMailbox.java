// Copyright © 2012-2017 Vaughn Vernon. All rights reserved.
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

import java.util.LinkedList;
import java.util.Queue;

public class SharedRingBufferMailbox implements Mailbox {
  private final Dispatcher dispatcher;
  private final int mailboxSize;
  private final Message[] messages;
  private final OverflowQueue overflowQueue;
  private int sendIndex;
  private int receiveIndex;

  public void close() {
    dispatcher.close();
    overflowQueue.close();
    clear();
  }

  public boolean isDelivering() {
    throw new UnsupportedOperationException("SharedRingBufferMailbox does not support this operation.");
  }

  public boolean delivering(final boolean flag) {
    throw new UnsupportedOperationException("SharedRingBufferMailbox does not support this operation.");
  }

  public int overflowCount() {
    return overflowQueue.messages.size();
  }

  public void send(final Message message) {
    synchronized(messages) {
      if (messages[sendIndex] == null) {
        messages[sendIndex] = message;
        if (++sendIndex >= mailboxSize) {
          sendIndex = 0;
        }
        if (dispatcher.requiresExecutionNotification()) {
          dispatcher.execute(this);
        }
      } else {
        overflowQueue.delayedSend(message);
        dispatcher.execute(this);
      }
    }
  }

  public Message receive() {
    final Message message = messages[receiveIndex];
    if (message != null) {
      messages[receiveIndex] = null;
      if (++receiveIndex >= mailboxSize) {
        receiveIndex = 0;
      }
      if (overflowQueue.isOverflowed()) {
        overflowQueue.execute();
      }
    }
    return message;
  }

  public void run() {
    throw new UnsupportedOperationException("SharedRingBufferMailbox does not support this operation.");
  }

  protected SharedRingBufferMailbox(final Dispatcher dispatcher, final int mailboxSize) {
    this.dispatcher = dispatcher;
    this.mailboxSize = mailboxSize;
    this.messages = new Message[mailboxSize];
    this.overflowQueue = new OverflowQueue();
    this.receiveIndex = 0;
    this.sendIndex = 0;
  }

  private boolean canSend() {
    int index = sendIndex;
    if (index >= mailboxSize) {
      index = 0;
    }
    
    return messages[index] == null;
  }
  
  private void clear() {
    for (int idx = 0; idx < mailboxSize; ++idx)
      messages[idx] = null;
  }

  private class OverflowQueue extends Thread {
    private final Backoff backoff;
    private final Queue<Message> messages;
    private boolean open;

    @Override
    public void run() {
      while (open) {
        if (canSend()) {
          Message delayed = messages.poll();
          if (delayed != null) {
            backoff.reset();
            send(delayed);
          } else {
            backoff.now();
          }
        } else {
          backoff.now();
        }
      }
    }

    private OverflowQueue() {
      backoff = new Backoff();
      messages = new LinkedList<Message>();
      open = false;
    }

    private void close() {
      open = false;
      messages.clear();
    }

    private void delayedSend(final Message message) {
      messages.add(message);

      if (!open) {
        open = true;
        start();
      } else {
        execute();
      }
    }

    private boolean isOverflowed() {
      return open && !messages.isEmpty();
    }
    
    private void execute() {
      interrupt();
    }
  }
}
