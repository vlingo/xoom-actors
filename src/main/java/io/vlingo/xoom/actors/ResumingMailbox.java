// Copyright © 2012-2023 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.actors;

public class ResumingMailbox implements Mailbox {
  private final Message message;

  public ResumingMailbox(final Message message) {
    this.message = message;
  }

  @Override
  public void run() {
    message.deliver();
  }

  @Override
  public void close() { }

  @Override
  public boolean isClosed() { return false; }

  @Override
  public boolean isDelivering() { return true; }

  @Override
  public int concurrencyCapacity() { return 0; }

  @Override
  public void resume(final String name) { }

  @Override
  public void send(final Message message) { }

  @Override
  public void suspendExceptFor(String name, Class<?>... overrides) { }

  @Override
  public boolean isSuspended() { return false; }

  @Override
  public Message receive() { return null; }

  @Override
  public int pendingMessages() { return 1; }
}
