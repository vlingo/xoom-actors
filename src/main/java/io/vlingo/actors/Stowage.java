// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;

public class Stowage {
  private Queue<Message> stowedMessages;
  private AtomicBoolean dispersing;
  private AtomicBoolean stowing;

  public Stowage() {
    this.dispersing = new AtomicBoolean(false);
    this.stowing = new AtomicBoolean(false);
    reset();
  }

  protected int count() {
    return stowedMessages.size();
  }

  protected void dump(final Logger logger) {
    for (final Message message : stowedMessages) {
      logger.log("STOWED: " + message);
    }
  }

  protected boolean hasMessages() {
    return !stowedMessages.isEmpty();
  }

  protected Message head() {
    if (stowedMessages.isEmpty()) {
      reset();
      return null;
    }
    return stowedMessages.poll();
  }

  protected void reset() {
    this.stowedMessages = new LinkedList<>();
    this.stowing.set(false);
    this.dispersing.set(false);
  }

  protected boolean isStowing() {
    return stowing.get();
  }

  protected void stowingMode() {
    this.stowing.set(true);
    this.dispersing.set(false);
  }

  protected boolean isDispersing() {
    return dispersing.get();
  }

  protected void dispersingMode() {
    this.stowing.set(false);
    this.dispersing.set(true);
  }

  @SuppressWarnings({ "rawtypes", "unchecked" })
  protected void stow(final Message message) {
    if (isStowing()) {
      stowedMessages.add(new StowedLocalMessage((LocalMessage) message));
    }
  }

  @SuppressWarnings({ "rawtypes", "unchecked" })
  protected Message swapWith(final Message newerMessage) {
    if (stowedMessages.isEmpty()) {
      reset();
      return newerMessage;
    }
    
    final Message olderMessage = head();
    stowedMessages.add(new StowedLocalMessage((LocalMessage) newerMessage));
    return olderMessage;
  }
}
