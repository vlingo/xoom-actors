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

  @Override
  public String toString() {
    return "Stowage[stowing=" + stowing.get() + ", dispersing=" + dispersing.get() +
           " messages=" + stowedMessages + "]";
  }

  int count() {
    return stowedMessages.size();
  }

  void dump(final Logger logger) {
    for (final Message message : stowedMessages) {
      logger.log("STOWED: " + message);
    }
  }

  boolean hasMessages() {
    return !stowedMessages.isEmpty();
  }

  Message head() {
    if (stowedMessages.isEmpty()) {
      reset();
      return null;
    }
    return stowedMessages.poll();
  }

  void reset() {
    this.stowedMessages = new LinkedList<>();
    this.stowing.set(false);
    this.dispersing.set(false);
  }

  boolean isStowing() {
    return stowing.get();
  }

  void stowingMode() {
    this.stowing.set(true);
    this.dispersing.set(false);
  }

  boolean isDispersing() {
    return dispersing.get();
  }

  void dispersingMode() {
    this.stowing.set(false);
    this.dispersing.set(true);
  }

  void restow(final Stowage other) {
    for (Message message = head(); message != null; message = head()) {
      other.stow(message);
    }
  }

  @SuppressWarnings({ "rawtypes", "unchecked" })
  void stow(final Message message) {
    if (isStowing()) {
      final Message toStow;
      if (message.isStowed()) {
        toStow = message;
      } else {
        toStow = new StowedLocalMessage((LocalMessage) message);
      }
      stowedMessages.add(toStow);
    }
  }

  @SuppressWarnings({ "rawtypes", "unchecked" })
  Message swapWith(final Message newerMessage) {
    if (stowedMessages.isEmpty()) {
      reset();
      return newerMessage;
    }
    
    final Message olderMessage = head();
    stowedMessages.add(new StowedLocalMessage((LocalMessage) newerMessage));
    return olderMessage;
  }
}
