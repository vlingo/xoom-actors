// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors;

import java.util.function.Consumer;

public class Stoppable__Proxy implements Stoppable {
  private final Actor actor;
  private final Stoppable typedActor;
  private final Mailbox mailbox;

  public Stoppable__Proxy(final Actor actor, final Mailbox mailbox) {
    this.actor = actor;
    this.typedActor = (Stoppable) actor;
    this.mailbox = mailbox;
  }

  @Override
  public boolean isStopped() {
    return actor.isStopped();
  }

  @Override
  public void stop() {
    final Consumer<Stoppable> consumer = (actor) -> actor.stop();
    mailbox.send(new LocalMessage<Stoppable>(actor, typedActor, consumer, "stop()"));
  }
}
