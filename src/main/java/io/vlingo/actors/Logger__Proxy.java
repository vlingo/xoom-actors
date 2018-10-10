// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors;

import io.vlingo.actors.Actor;
import io.vlingo.actors.DeadLetter;
import io.vlingo.actors.LocalMessage;
import io.vlingo.actors.Mailbox;

public class Logger__Proxy implements Logger {

  private static final String nameRepresentation1 = "name()";
  private static final String logRepresentation2 = "log(java.lang.String)";
  private static final String logRepresentation3 = "log(java.lang.String, java.lang.Throwable)";
  private static final String closeRepresentation4 = "close()";
  private static final String isEnabledRepresentation5 = "isEnabled()";

  private final Actor actor;
  private final Mailbox mailbox;

  public Logger__Proxy(final Actor actor, final Mailbox mailbox){
    this.actor = actor;
    this.mailbox = mailbox;
  }

  public String name() {
    if (!actor.isStopped()) {
      final java.util.function.Consumer<Logger> consumer = (actor) -> actor.name();
      if (mailbox.isPreallocated()) { mailbox.send(actor, Logger.class, consumer, null, nameRepresentation1); }
      else { mailbox.send(new LocalMessage<Logger>(actor, Logger.class, consumer, nameRepresentation1)); }
    } else {
      actor.deadLetters().failedDelivery(new DeadLetter(actor, nameRepresentation1));
    }
    return null;
  }
  public void log(java.lang.String arg0) {
    if (!actor.isStopped()) {
      final java.util.function.Consumer<Logger> consumer = (actor) -> actor.log(arg0);
      if (mailbox.isPreallocated()) { mailbox.send(actor, Logger.class, consumer, null, logRepresentation2); }
      else { mailbox.send(new LocalMessage<Logger>(actor, Logger.class, consumer, logRepresentation2)); }
    } else {
      actor.deadLetters().failedDelivery(new DeadLetter(actor, logRepresentation2));
    }
  }
  public void log(java.lang.String arg0, java.lang.Throwable arg1) {
    if (!actor.isStopped()) {
      final java.util.function.Consumer<Logger> consumer = (actor) -> actor.log(arg0, arg1);
      if (mailbox.isPreallocated()) { mailbox.send(actor, Logger.class, consumer, null, logRepresentation3); }
      else { mailbox.send(new LocalMessage<Logger>(actor, Logger.class, consumer, logRepresentation3)); }
    } else {
      actor.deadLetters().failedDelivery(new DeadLetter(actor, logRepresentation3));
    }
  }
  public void close() {
    if (!actor.isStopped()) {
      final java.util.function.Consumer<Logger> consumer = (actor) -> actor.close();
      if (mailbox.isPreallocated()) { mailbox.send(actor, Logger.class, consumer, null, closeRepresentation4); }
      else { mailbox.send(new LocalMessage<Logger>(actor, Logger.class, consumer, closeRepresentation4)); }
    } else {
      actor.deadLetters().failedDelivery(new DeadLetter(actor, closeRepresentation4));
    }
  }
  public boolean isEnabled() {
    if (!actor.isStopped()) {
      final java.util.function.Consumer<Logger> consumer = (actor) -> actor.isEnabled();
      if (mailbox.isPreallocated()) { mailbox.send(actor, Logger.class, consumer, null, isEnabledRepresentation5); }
      else { mailbox.send(new LocalMessage<Logger>(actor, Logger.class, consumer, isEnabledRepresentation5)); }
    } else {
      actor.deadLetters().failedDelivery(new DeadLetter(actor, isEnabledRepresentation5));
    }
    return false;
  }
}
