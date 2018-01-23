// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.actors;

import java.util.function.Consumer;

public class Startable__Proxy implements Startable {
  private final Actor actor;
  private final Mailbox mailbox;

  public Startable__Proxy(final Actor actor, final Mailbox mailbox) {
    this.actor = actor;
    this.mailbox = mailbox;
  }

  @Override
  public void start() {
    final Consumer<Startable> consumer = (actor) -> actor.start();
    mailbox.send(new LocalMessage<Startable>(actor, Startable.class, consumer, "start()"));
  }
}
