// Copyright © 2012-2023 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.actors;

import java.util.Optional;

import io.vlingo.xoom.common.Completes;
import io.vlingo.xoom.common.SerializableConsumer;

public class DirectoryScanner__Proxy implements DirectoryScanner {

  private static final String actorOfRepresentation1 = "actorOf(io.vlingo.xoom.actors.Address, java.lang.Class<T>)";
  private static final String actorOfRepresentation2 = "actorOf(io.vlingo.xoom.actors.Address, java.lang.Class<T>, io.vlingo.xoom.actors.Definition)";
  private static final String maybeActorOfRepresentation3 = "maybeActorOf(io.vlingo.xoom.actors.Address, java.lang.Class<T>)";

  private final Actor actor;
  private final Mailbox mailbox;

  public DirectoryScanner__Proxy(final Actor actor, final Mailbox mailbox){
    this.actor = actor;
    this.mailbox = mailbox;
  }

  @Override
  public <T> Completes<T> actorOf(final java.lang.Class<T> arg0, final io.vlingo.xoom.actors.Address arg1) {
    if (!actor.isStopped()) {
      final SerializableConsumer<DirectoryScanner> consumer = (actor) -> actor.actorOf(arg0, arg1);
      final Completes<T> completes = Completes.using(actor.scheduler());
      if (mailbox.isPreallocated()) { mailbox.send(actor, DirectoryScanner.class, consumer, Returns.value(completes), actorOfRepresentation1); }
      else { mailbox.send(new LocalMessage<DirectoryScanner>(actor, DirectoryScanner.class, consumer, Returns.value(completes), actorOfRepresentation1)); }
      return completes;
    } else {
      actor.deadLetters().failedDelivery(new DeadLetter(actor, actorOfRepresentation1));
    }
    return null;
  }

  @Override
  public <T> Completes<T> actorOf(final java.lang.Class<T> arg0, final io.vlingo.xoom.actors.Address arg1, final io.vlingo.xoom.actors.Definition arg2) {
    if (!actor.isStopped()) {
      final SerializableConsumer<DirectoryScanner> consumer = (actor) -> actor.actorOf(arg0, arg1, arg2);
      final Completes<T> completes = Completes.using(actor.scheduler());
      if (mailbox.isPreallocated()) { mailbox.send(actor, DirectoryScanner.class, consumer, Returns.value(completes), actorOfRepresentation2); }
      else { mailbox.send(new LocalMessage<DirectoryScanner>(actor, DirectoryScanner.class, consumer, Returns.value(completes), actorOfRepresentation2)); }
      return completes;
    } else {
      actor.deadLetters().failedDelivery(new DeadLetter(actor, actorOfRepresentation2));
    }
    return null;
  }

  @Override
  public <T> Completes<Optional<T>> maybeActorOf(final Class<T> arg0, final Address arg1) {
    if (!actor.isStopped()) {
      final SerializableConsumer<DirectoryScanner> consumer = (actor) -> actor.maybeActorOf(arg0, arg1);
      final Completes<Optional<T>> completes = Completes.using(actor.scheduler());
      if (mailbox.isPreallocated()) { mailbox.send(actor, DirectoryScanner.class, consumer, Returns.value(completes), maybeActorOfRepresentation3); }
      else { mailbox.send(new LocalMessage<DirectoryScanner>(actor, DirectoryScanner.class, consumer, Returns.value(completes), maybeActorOfRepresentation3)); }
      return completes;
    } else {
      actor.deadLetters().failedDelivery(new DeadLetter(actor, maybeActorOfRepresentation3));
    }
    return null;
  }
}
