// Copyright © 2012-2021 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.actors;

import java.util.LinkedList;
import java.util.Queue;

public class Stowage {
  private Queue<Message> stowedMessages;
  private volatile boolean dispersing;
  private volatile boolean stowing;

  public Stowage() {
    this.dispersing = false;
    this.stowing = false;
    reset();
  }

  @Override
  public String toString() {
    return "Stowage[stowing=" + stowing + ", dispersing=" + dispersing +
           " messages=" + stowedMessages + "]";
  }

  int count() {
    return stowedMessages.size();
  }

  void dump(final Logger logger) {
    for (final Message message : stowedMessages) {
      logger.debug("STOWED: " + message);
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
    this.stowing = false;
    this.dispersing = false;
  }

  boolean isStowing() {
    return stowing;
  }

  void stowingMode() {
    this.stowing = true;
    this.dispersing = false;
  }

  boolean isDispersing() {
    return dispersing;
  }

  void dispersingMode() {
    this.stowing = false;
    this.dispersing = true;
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
