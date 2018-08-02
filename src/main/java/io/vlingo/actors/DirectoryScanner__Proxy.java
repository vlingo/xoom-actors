// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors;

import java.util.function.Consumer;

import io.vlingo.actors.Actor;
import io.vlingo.actors.BasicCompletes;
import io.vlingo.actors.Completes;
import io.vlingo.actors.DeadLetter;
import io.vlingo.actors.LocalMessage;
import io.vlingo.actors.Mailbox;

public class DirectoryScanner__Proxy implements DirectoryScanner {

  private static final String actorOfRepresentation1 = "actorOf(io.vlingo.actors.Address, java.lang.Class<T>)";

  private final Actor actor;
  private final Mailbox mailbox;

  public DirectoryScanner__Proxy(final Actor actor, final Mailbox mailbox){
    this.actor = actor;
    this.mailbox = mailbox;
  }

  @Override
  public <T> Completes<T> actorOf(final io.vlingo.actors.Address arg0, final java.lang.Class<T> arg1) {
    if (!actor.isStopped()) {
      final Consumer<DirectoryScanner> consumer = (actor) -> actor.actorOf(arg0, arg1);
      final Completes<T> completes = new BasicCompletes<>(actor.scheduler());
      mailbox.send(new LocalMessage<DirectoryScanner>(actor, DirectoryScanner.class, consumer, completes, actorOfRepresentation1));
      return completes;
    } else {
      actor.deadLetters().failedDelivery(new DeadLetter(actor, actorOfRepresentation1));
    }
    return null;
  }
}
